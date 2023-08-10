package com.paypal.jsse.benchmark.client;

import com.paypal.infra.ssl.PayPalSSLSession;
import com.paypal.jsse.benchmark.client.metrics.Metric;
import com.paypal.jsse.benchmark.client.metrics.MetricsRegistry;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;

public class ApacheHttpsClient extends HttpsClient<CloseableHttpClient> {

    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpsClient.class);

    public ApacheHttpsClient(MetricsRegistry metricsRegistry) {
        super(metricsRegistry);
    }

    @Override
    public CloseableHttpClient createHttpsClient(String host, int port, SSLContext sslContext, MetricsRegistry metricsRegistry) {
        final Metric.SSLMetric sslMetrics = new Metric.SSLMetric();
        metricsRegistry.addMetric(sslMetrics);
        return HttpClients.custom()
                .setSSLSocketFactory(new CustomSSLSocketFactory(sslContext, sslMetrics))
                .build();
    }

    @Override
    protected CloseableHttpClient createHttpsClient(String host, int port, SSLContext sslContext) {
        return HttpClients.custom()
                .setSSLSocketFactory(new CustomSSLSocketFactory(sslContext))
                .build();
    }

    @Override
    public String executeHttpsCall(String path) {
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

        public CustomSSLSocketFactory(final SSLContext sslContext) {
            this(sslContext, null);
        }

        public CustomSSLSocketFactory(final SSLContext sslContext,
                                      final Metric.SSLMetric sslMetrics) {
            super(sslContext, new CustomAllowAllHostnameVerifier());
            this.sslMetrics = sslMetrics;
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
            if(logger.isDebugEnabled()) {
                final SSLSession session = ((SSLSocket) sslSocket).getSession();
                logSessionInfo(session instanceof PayPalSSLSession, session);
            }
            return sslSocket;
        }

        public Socket recordSslHandshakeTime(final Callable<Socket> handshakeFn) {
            if (this.sslMetrics != null) {
                final long startTime = System.nanoTime();
                try {
                    return handshakeFn.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    this.sslMetrics.addMetric(elapsedTime(startTime).doubleValue());
                }
            } else {
                try {
                    return handshakeFn.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class CustomAllowAllHostnameVerifier implements X509HostnameVerifier {

        public final void verify(final String host, final SSLSocket ssl)
                throws IOException {
            //Allow everything
        }

        public final void verify(final String host, final X509Certificate cert)
                throws SSLException {
            verify(host, null, null);
        }


        public final boolean verify(final String host, final SSLSession session) {
            return true;
        }

        public final void verify(
                final String host,
                final String[] cns,
                final String[] subjectAlts) {
            // Allow everything
        }

        @Override
        public final String toString() {
            return "CUSTOM_ALLOW_ALL_HOSTNAME_VERIFIER";
        }
    }
}
