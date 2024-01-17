package com.paypal.jsse.tester.tests.jmh;

import com.paypal.jsse.test.ssl.MockSSLContextFactory;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import org.openjdk.jmh.annotations.Benchmark;
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

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@State(Scope.Benchmark)
public class HttpsCallBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(HttpsCallBenchmark.class);

    private HttpClient client;
    private DisposableServer server;

    private static AtomicLong servedRequests = new AtomicLong(0l);

    private SslContext nettySslContext(final SSLContext sslContext, boolean isClient) {
        return new JdkSslContext(
                sslContext,
                isClient,
                null,
                IdentityCipherSuiteFilter.INSTANCE,
                null,
                ClientAuth.NONE, // this value is required for the constructor but is not used for clients
                null,
                false);
    }
    @Setup
    public void setup() {
        MockSSLContextFactory factory = new MockSSLContextFactory();
        this.server = HttpServer
                .create()
                .port(6443)
                .secure(SslProvider.builder().sslContext(nettySslContext(factory.sslContext(false), false)).build())
                .route(routes -> routes
                        .get("/hi",
                                (req, res) -> {
                                    servedRequests.incrementAndGet();
                                    return res.sendString(Mono.just("Hello world").delayElement(Duration.ofMillis(100)));
                                }))
                .bindNow();
        client = HttpClient.create(ConnectionProvider.newConnection())
                .keepAlive(false)
                .secure(SslProvider.builder().sslContext(nettySslContext(factory.sslContext(true), true)).build())
                .baseUrl(String.format("https://%s:%s", "localhost", "6443"));
    }

    @Benchmark
    public void execute() {
        //logger.info("execute : " + servedRequests.get());
        final String response = client
                .get()
                .uri("/hi")
                .responseSingle((resp, bytes) -> bytes.asString())
                .doOnError(throwable -> logger.debug(throwable.getMessage(), throwable))
                .onErrorReturn("failed")
                .block();
        if(logger.isDebugEnabled()) {
            logger.debug("Response: " + response);
        }
    }

    @TearDown
    public void cleanup() {
        if(this.server != null) {
            this.server.disposeNow();
        }

        logger.info("total requests served : " + servedRequests.get());
    }

}
