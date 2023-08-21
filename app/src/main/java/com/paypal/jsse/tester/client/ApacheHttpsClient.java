package com.paypal.jsse.tester.client;

import com.paypal.infra.ssl.PayPalSSLSession;
import com.paypal.jsse.tester.client.metrics.Metric;
import com.paypal.jsse.tester.client.metrics.MetricsRegistry;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

public class ApacheHttpsClient extends HttpsClient<CloseableHttpClient> {

    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpsClient.class);

    public ApacheHttpsClient(final MetricsRegistry metricsRegistry,
                             final Boolean resumptionTest) {
        super(metricsRegistry, resumptionTest);
    }

    @Override
    public CloseableHttpClient createHttpsClient(final String host,
                                                 final int port,
                                                 final SSLContext sslContext) {
        return HttpClients
                .custom()
                .setSSLSocketFactory(createCustomSSLSocketFactory(sslContext))
                .build();
    }

    private CustomSSLSocketFactory createCustomSSLSocketFactory(final SSLContext sslContext) {
        if(metricsRegistry != null) {
            final Metric.SSLMetric sslMetrics = new Metric.SSLMetric();
            this.metricsRegistry.addMetric(sslMetrics);
            return new CustomSSLSocketFactory(sslContext, sslMetrics, resumptionTest);
        } else {
            return new CustomSSLSocketFactory(sslContext, resumptionTest);
        }
    }

    @Override
    public String executeHttpsCall(final String path) {
        try {
            final String url = baseUrl + "/" + path;
            final HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Connection", "close");
            HttpResponse response = client.execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    String clientName() {
        return "Apache";
    }

    public static class CustomSSLSocketFactory extends SSLConnectionSocketFactory {

        private final Metric.SSLMetric sslMetrics;

        private final boolean resumptionTest;

        public CustomSSLSocketFactory(final SSLContext sslContext,
                                      final boolean resumptionTest) {
            this(sslContext, null, resumptionTest);
        }

        public CustomSSLSocketFactory(final SSLContext sslContext,
                                      final Metric.SSLMetric sslMetrics,
                                      final boolean resumptionTest) {
            super(sslContext, new NoopHostnameVerifier());
            this.sslMetrics = sslMetrics;
            this.resumptionTest = resumptionTest;
        }

        @Override
        public Socket connectSocket(final int connectTimeout,
                                    final Socket socket,
                                    final HttpHost host,
                                    final InetSocketAddress remoteAddress,
                                    final InetSocketAddress localAddress,
                                    final HttpContext context) throws IOException {
            Args.notNull(host, "HTTP host");
            Args.notNull(remoteAddress, "Remote address");
            final Socket sock = socket != null ? socket : createSocket(context);
            if (localAddress != null) {
                sock.bind(localAddress);
            }
            try {
                if (connectTimeout > 0 && sock.getSoTimeout() == 0) {
                    sock.setSoTimeout(connectTimeout);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Connecting socket to " + remoteAddress + " with timeout " + connectTimeout);
                }
                sock.connect(remoteAddress, connectTimeout);
            } catch (final IOException ex) {
                try {
                    sock.close();
                } catch (final IOException ignore) {
                }
                throw ex;
            }
            // Setup SSL layering if necessary
            if (sock instanceof SSLSocket) {
                final SSLSocket sslsock = (SSLSocket) sock;
                logger.debug("Starting handshake");
                recordSslHandshakeTime(() -> {
                    try {
                        sslsock.startHandshake();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return sslsock;
                });
                return sock;
            }
            final Socket sslSocket = recordSslHandshakeTime(() -> createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context));
            if(logger.isDebugEnabled() || resumptionTest) {
                final SSLSession session = ((SSLSocket) sslSocket).getSession();
                logSessionInfo(session instanceof PayPalSSLSession, session);
            }
            return sslSocket;
        }

        public Socket recordSslHandshakeTime(final Callable<Socket> handshakeFn) {
            try {
                if (this.sslMetrics != null) {
                    final long startTime = System.nanoTime();
                    final Socket socket = handshakeFn.call();
                    this.sslMetrics.addMetric(elapsedTime(startTime).doubleValue());
                    return socket;
                } else {
                    return handshakeFn.call();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
