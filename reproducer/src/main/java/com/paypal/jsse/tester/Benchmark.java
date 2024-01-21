package com.paypal.jsse.tester;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class Benchmark {

    private static final Logger logger = LoggerFactory.getLogger(Benchmark.class);

    private HttpClient client;

    private Tomcat tomcat;
    private DisposableServer server;

    private static AtomicLong servedRequests = new AtomicLong(0l);

    @Setup
    public void setup() {
        if("netty".equals(TestExecutor.readProperty("server.type",
                        String.class,
                        "netty"))){
            startNettyServer();
        } else {
            startTomcatServer();
        }
        client = HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(false)
                .secure(SslProvider.builder().sslContext(SSLContextFactory.nettySslContext(true)).build())
                .baseUrl(String.format("https://%s:%s", "localhost", "6443"));
    }

    private void startNettyServer(){
        this.server = HttpServer
                .create()
                .port(6443)
                .secure(SslProvider.builder().sslContext(SSLContextFactory.nettySslContext(false)).build())
                .route(routes -> routes
                        .get("/hi",
                                (req, res) -> {
                                    servedRequests.incrementAndGet();
                                    return res.sendString(Mono.just("Hello world").delayElement(Duration.ofMillis(100)));
                                }))
                .bindNow();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void execute() {
        final String response = client
                .get()
                .uri("/hi")
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.info(throwable.getMessage(), throwable))
                .onErrorReturn("failed")
                .block();
        if (logger.isDebugEnabled()){
            System.out.println("Response : " + response);
        }
    }

    @TearDown
    public void cleanup() {
        if(this.server != null) {
            this.server.disposeNow();
        }
        if(this.tomcat != null) {
            try {
                this.tomcat.stop();
            }catch (Exception e){

            }
        }

        logger.info("Total requests served : " + servedRequests.get());
    }

    private void startTomcatServer(){
        try {
            tomcat = new Tomcat();
            tomcat.setBaseDir("temp");
            tomcat.setPort(8080);
            tomcat.setHostname("localhost");
            tomcat.getHost().setAppBase(".");

            String contextPath = "/";
            String docBase = new File(".").getAbsolutePath();

            // Configure the HTTPS connector
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            final String protectedFilePath = Objects.requireNonNull(SSLContextFactory.class.getClassLoader().getResource("mock-protected")).getPath();

            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(6443);
            protocol.setSSLEnabled(true);
            protocol.setKeystoreFile(new File(protectedFilePath + "/azultest.jks").getAbsolutePath());
            protocol.setKeystorePass("123456789");
            protocol.setTruststoreFile(new File(protectedFilePath + "/azultest.jks").getAbsolutePath());
            protocol.setTruststorePass("123456789");
            protocol.setKeyAlias("azultest");

            tomcat.getService().addConnector(connector);

            Context context = tomcat.addContext(contextPath, docBase);

            HttpServlet servlet = new HttpServlet() {
                @Override
                protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                        throws ServletException, IOException {
                    PrintWriter writer = resp.getWriter();
                    servedRequests.incrementAndGet();
                    writer.println("Hello World From Tomcat!");
                }
            };

            tomcat.addServlet(contextPath, "ServletHi", servlet);
            context.addServletMappingDecoded("/hi", "ServletHi");

            tomcat.start();

            logger.info("Started Tomcat Server");
        }catch (Exception e) {
            logger.error("error in staring the server", e);
        }
    }

    public static void main(String[] args){
        Benchmark httpsCallBenchmark = new Benchmark();

        httpsCallBenchmark.setup();
        IntStream.range(0, 10).forEach(e -> httpsCallBenchmark.execute());
        httpsCallBenchmark.cleanup();
    }
}
