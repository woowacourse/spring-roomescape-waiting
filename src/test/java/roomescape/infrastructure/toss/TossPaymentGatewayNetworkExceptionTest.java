package roomescape.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import roomescape.domain.vo.PaymentConfirmation;

class TossPaymentGatewayNetworkExceptionTest {
    private static final String ORDER_ID = "order-id";
    private static final String IDEMPOTENCY_KEY = ORDER_ID;

    @Test
    @DisplayName("토스 연결 단계에서 실패하면 ResourceAccessException 안에 ConnectException이 들어 있다")
    void confirm_connectionRefused() throws IOException {
        TossPaymentGateway gateway = tossPaymentGateway("http://localhost:" + unusedPort());

        Throwable throwable = catchThrowable(() -> gateway.confirm(
                paymentConfirmation()
        ));

        assertThat(throwable).isInstanceOf(ResourceAccessException.class);
        assertThat(rootCause(throwable)).isInstanceOf(ConnectException.class);
    }

    @Test
    @DisplayName("토스 응답이 read timeout보다 늦으면 RestClientException 안에 SocketTimeoutException이 들어 있다")
    void confirm_readTimeout() throws IOException {
        HttpServer slowServer = slowServer();
        slowServer.start();

        try {
            TossPaymentGateway gateway = tossPaymentGateway("http://localhost:" + slowServer.getAddress().getPort());

            Throwable throwable = catchThrowable(() -> gateway.confirm(
                    paymentConfirmation()
            ));

            assertThat(throwable).isInstanceOf(RestClientException.class);
            assertThat(rootCause(throwable)).isInstanceOf(SocketTimeoutException.class);
        } finally {
            slowServer.stop(0);
        }
    }

    @Test
    @DisplayName("토스 승인 요청에 주문에 고정된 Idempotency-Key 헤더를 보낸다")
    void confirm_sendsIdempotencyKeyHeader() throws IOException {
        AtomicReference<String> idempotencyKey = new AtomicReference<>();
        HttpServer server = successServer(idempotencyKey);
        server.start();

        try {
            TossPaymentGateway gateway = tossPaymentGateway("http://localhost:" + server.getAddress().getPort());

            gateway.confirm(paymentConfirmation());

            assertThat(idempotencyKey.get()).isEqualTo(IDEMPOTENCY_KEY);
            assertThat(idempotencyKey.get()).hasSizeLessThanOrEqualTo(300);
        } finally {
            server.stop(0);
        }
    }

    private TossPaymentGateway tossPaymentGateway(String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(100));
        requestFactory.setReadTimeout(Duration.ofMillis(100));

        TossPaymentGateway gateway = new TossPaymentGateway(
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .requestFactory(requestFactory)
                        .build(),
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(gateway, "secretKey", "test-secret-key");
        return gateway;
    }

    private HttpServer slowServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v1/payments/confirm", exchange -> {
            try {
                Thread.sleep(300);
                byte[] body = """
                        {"paymentKey":"payment-key","orderId":"order-id","totalAmount":10000}
                        """.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        return server;
    }

    private HttpServer successServer(AtomicReference<String> idempotencyKey) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v1/payments/confirm", exchange -> {
            idempotencyKey.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            byte[] body = """
                    {"paymentKey":"payment-key","orderId":"order-id","totalAmount":10000}
                    """.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        return server;
    }

    private int unusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation("payment-key", ORDER_ID, 10_000L, IDEMPOTENCY_KEY);
    }
}
