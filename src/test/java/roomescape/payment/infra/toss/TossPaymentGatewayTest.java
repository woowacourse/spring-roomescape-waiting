package roomescape.payment.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.exception.PaymentConfirmationPendingException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class TossPaymentGatewayTest {

    @Test
    @DisplayName("응답 읽기 타임아웃은 결제 결과 확인 필요 예외로 안내한다.")
    void confirm_pending_whenReadTimeout() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        HttpServer server = delayedBodyServer(Duration.ofMillis(300), executor);
        server.start();

        try {
            TossPaymentGateway gateway = gateway(
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    Duration.ofMillis(100),
                    Duration.ofMillis(50)
            );

            assertThatThrownBy(() -> gateway.confirm(
                    new PaymentConfirmation("payment-key", "order-id", 1000L, "idempotency-key")
            ))
                    .isInstanceOf(PaymentConfirmationPendingException.class)
                    .hasMessage("결제 승인 요청에 응답이 없습니다. 승인 여부가 확인되지 않았습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.");
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("연결 실패는 결제 결과 확인 필요 예외로 안내한다.")
    void confirm_pending_whenConnectionFails() throws IOException {
        TossPaymentGateway gateway = gateway(
                "http://127.0.0.1:" + unusedPort(),
                Duration.ofMillis(100),
                Duration.ofMillis(100)
        );

        assertThatThrownBy(() -> gateway.confirm(
                new PaymentConfirmation("payment-key", "order-id", 1000L, "idempotency-key")
        ))
                .isInstanceOf(PaymentConfirmationPendingException.class)
                .hasMessage("결제 승인 서버에 연결하지 못했습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.");
    }

    @Test
    @DisplayName("에러 응답 본문을 읽다가 타임아웃이 나도 결제 결과 확인 필요 예외로 안내한다.")
    void confirm_pending_whenErrorBodyReadTimeout() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        HttpServer server = delayedErrorBodyServer(Duration.ofMillis(300), executor);
        server.start();

        try {
            TossPaymentGateway gateway = gateway(
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    Duration.ofMillis(100),
                    Duration.ofMillis(50)
            );

            assertThatThrownBy(() -> gateway.confirm(
                    new PaymentConfirmation("payment-key", "order-id", 1000L, "idempotency-key")
            ))
                    .isInstanceOf(PaymentConfirmationPendingException.class)
                    .hasMessage("결제 승인 요청에 응답이 없습니다. 승인 여부가 확인되지 않았습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.");
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("승인 요청에는 저장된 멱등키를 Idempotency-Key 헤더로 보낸다.")
    void confirm_success_sendsIdempotencyKeyHeader() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<String> idempotencyKeyHeader = new AtomicReference<>();
        HttpServer server = successServer(idempotencyKeyHeader, executor);
        server.start();

        try {
            TossPaymentGateway gateway = gateway(
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    Duration.ofMillis(100),
                    Duration.ofMillis(100)
            );

            gateway.confirm(new PaymentConfirmation("payment-key", "order-id", 1000L, "fixed-idempotency-key"));

            assertThat(idempotencyKeyHeader.get()).isEqualTo("fixed-idempotency-key");
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("실제 RestClient 연결 거부는 ResourceAccessException과 ConnectException 원인으로 표면화된다.")
    void restClient_exceptionShape_whenConnectionRefused() throws IOException {
        RestClient restClient = restClient(
                "http://127.0.0.1:" + unusedPort(),
                Duration.ofMillis(100),
                Duration.ofMillis(100)
        );

        Throwable throwable = catchThrowable(() -> confirmAsString(restClient));

        assertThat(throwable)
                .isInstanceOf(ResourceAccessException.class)
                .hasRootCauseInstanceOf(java.net.ConnectException.class);
        assertThat(causeTypeNames(throwable))
                .containsExactly(
                        ResourceAccessException.class.getName(),
                        java.net.ConnectException.class.getName()
                );
        assertThat(causeMessages(throwable).getLast())
                .containsIgnoringCase("Connection refused");
    }

    @Test
    @Timeout(5)
    @DisplayName("실제 RestClient 연결 타임아웃은 ResourceAccessException과 SocketTimeoutException 원인으로 표면화된다.")
    void restClient_exceptionShape_whenConnectTimeout() {
        RestClient restClient = restClient(
                "http://192.0.2.1:81",
                Duration.ofMillis(100),
                Duration.ofSeconds(2)
        );

        Throwable throwable = catchThrowable(() -> confirmAsString(restClient));

        assertThat(throwable)
                .isInstanceOf(ResourceAccessException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        assertThat(causeTypeNames(throwable))
                .containsExactly(
                        ResourceAccessException.class.getName(),
                        SocketTimeoutException.class.getName()
                );
        assertThat(rootCause(throwable).getMessage())
                .containsIgnoringCase("Connect timed out");
        assertThat(causeMessages(throwable).getLast())
                .containsIgnoringCase("Connect timed out");
    }

    @Test
    @DisplayName("실제 RestClient 응답 본문 읽기 타임아웃은 RestClientException과 SocketTimeoutException 원인으로 표면화된다.")
    void restClient_exceptionShape_whenReadTimeout() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        HttpServer server = delayedBodyServer(Duration.ofMillis(300), executor);
        server.start();

        try {
            RestClient restClient = restClient(
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    Duration.ofMillis(100),
                    Duration.ofMillis(50)
            );

            Throwable throwable = catchThrowable(() -> confirmAsResponse(restClient));

            assertThat(throwable)
                    .isExactlyInstanceOf(RestClientException.class)
                    .hasRootCauseInstanceOf(SocketTimeoutException.class);
            assertThat(causeTypeNames(throwable))
                    .containsExactly(
                            RestClientException.class.getName(),
                            SocketTimeoutException.class.getName()
                    );
            assertThat(rootCause(throwable).getMessage()).containsIgnoringCase("Read timed out");
            assertThat(causeMessages(throwable).getLast()).containsIgnoringCase("Read timed out");
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private TossPaymentGateway gateway(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        PaymentProperties properties = new PaymentProperties();
        PaymentProperties.Toss toss = new PaymentProperties.Toss();
        toss.setBaseUrl(baseUrl);
        toss.setSecretKey("test_secret");
        toss.setConnectTimeout(connectTimeout);
        toss.setReadTimeout(readTimeout);
        properties.setToss(toss);

        return new TossPaymentGateway(RestClient.builder(), new ObjectMapper(), properties);
    }

    private RestClient restClient(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    private String confirmAsString(RestClient restClient) {
        return restClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(String.class);
    }

    private TossPaymentConfirmResponse confirmAsResponse(RestClient restClient) {
        return restClient.post()
                .uri("/v1/payments/confirm")
                .retrieve()
                .body(TossPaymentConfirmResponse.class);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private List<String> causeTypeNames(Throwable throwable) {
        return causeChain(throwable).stream()
                .map(cause -> cause.getClass().getName())
                .toList();
    }

    private List<String> causeMessages(Throwable throwable) {
        return causeChain(throwable).stream()
                .map(Throwable::getMessage)
                .toList();
    }

    private List<Throwable> causeChain(Throwable throwable) {
        List<Throwable> causes = new ArrayList<>();
        Throwable current = throwable;
        while (current != null) {
            causes.add(current);
            current = current.getCause();
        }
        return causes;
    }

    private HttpServer successServer(
            AtomicReference<String> idempotencyKeyHeader,
            ExecutorService executor
    ) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/v1/payments/confirm", exchange -> {
            try {
                idempotencyKeyHeader.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
                byte[] responseBody = """
                        {"paymentKey":"payment-key","orderId":"order-id","totalAmount":1000}
                        """.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBody.length);
                exchange.getResponseBody().write(responseBody);
            } catch (IOException ignored) {
            } finally {
                exchange.close();
            }
        });
        return server;
    }

    private HttpServer delayedBodyServer(Duration delay, ExecutorService executor) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/v1/payments/confirm", exchange -> {
            try {
                byte[] responseBody = """
                        {"paymentKey":"payment-key","orderId":"order-id","totalAmount":1000}
                        """.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBody.length);
                exchange.getResponseBody().flush();
                Thread.sleep(delay.toMillis());
                exchange.getResponseBody().write(responseBody);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
            } finally {
                exchange.close();
            }
        });
        return server;
    }

    private HttpServer delayedErrorBodyServer(Duration delay, ExecutorService executor) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/v1/payments/confirm", exchange -> {
            try {
                byte[] responseBody = """
                        {"code":"REJECT_CARD_PAYMENT","message":"카드 결제가 거절되었습니다."}
                        """.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, responseBody.length);
                Thread.sleep(delay.toMillis());
                exchange.getResponseBody().write(responseBody);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
            } finally {
                exchange.close();
            }
        });
        return server;
    }

    private int unusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
