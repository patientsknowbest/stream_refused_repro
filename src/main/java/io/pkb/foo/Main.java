package io.pkb.foo;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int N_THREADS = 256;
    // Via nginx container
    private static final String BASE_URL = "https://localhost:8443/";

    private static final int MAX_IDLE_CONNECTIONS = 20;
    private static final Duration KEEP_ALIVE = Duration.ofSeconds(5);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(1);
    private static final boolean RETRY_FAILURE  = false;
    private static final List<Protocol> PROTOCOLS = List.of(Protocol.HTTP_1_1, Protocol.HTTP_2);

    public static void main(String[] args) throws InterruptedException {
        var client = okHttpClient();
        Thread[] threads = new Thread[N_THREADS];
        for (int i = 0; i < N_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    while (true) {
                        try (var response = client.newCall(new Request.Builder()
                                        .url(BASE_URL)
                                        .get()
                                        .build())
                                .execute()) {
                            System.out.println(String.format("response code=[%d]", response.code()));
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < N_THREADS; i++) {
            threads[i].join();
        }
    }


    private static OkHttpClient okHttpClient() {
        try {
            // Trust all certs
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // same config as we are on prod
            var pool = new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE.toMillis(), TimeUnit.MILLISECONDS);
            return new OkHttpClient()
                    .newBuilder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                    .readTimeout(READ_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                    .writeTimeout(WRITE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(RETRY_FAILURE)
                    .connectionPool(pool)
                    .protocols(PROTOCOLS)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
