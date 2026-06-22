package roomescape.payment.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.exception.PaymentApprovalUnknownException;
import roomescape.payment.exception.PaymentCommunicationException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class TossPaymentGatewayTest {

    private static final String BASE_URL = "https://api.tosspayments.com";

    @Test
    @DisplayName("응답 읽기 타임아웃은 결제 승인 결과 불명 예외로 구분한다")
    void throwUnknownExceptionWhenReadTimeoutOccurs() {
        final TestGateway testGateway = createTestGateway();
        testGateway.server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "idempotency-key"))
                .andRespond(request -> {
                    throw new SocketTimeoutException("Read timed out");
                });

        assertThatThrownBy(() -> testGateway.gateway.confirm(confirmation()))
                .isInstanceOf(PaymentApprovalUnknownException.class)
                .hasMessageContaining("결제 승인 응답을 받지 못했습니다.");

        testGateway.server.verify();
    }

    @Test
    @DisplayName("연결 실패는 결제 승인 서버 통신 실패 예외로 구분한다")
    void throwCommunicationExceptionWhenConnectFails() {
        final TestGateway testGateway = createTestGateway();
        testGateway.server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Idempotency-Key", "idempotency-key"))
                .andRespond(request -> {
                    throw new ConnectException("Connection refused");
                });

        assertThatThrownBy(() -> testGateway.gateway.confirm(confirmation()))
                .isInstanceOf(PaymentCommunicationException.class)
                .hasMessageContaining("결제 승인 서버에 연결하지 못했습니다.");

        testGateway.server.verify();
    }

    private TestGateway createTestGateway() {
        final RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL);
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder)
                .build();
        final TossPaymentGateway gateway = new TossPaymentGateway(
                builder.build(),
                new TossPaymentProperties(
                        BASE_URL,
                        "client-key",
                        "secret-key",
                        Duration.ofSeconds(3),
                        Duration.ofSeconds(5),
                        null
                ),
                new ObjectMapper()
        );

        return new TestGateway(gateway, server);
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation(
                "payment-key",
                "order-id",
                10000,
                "idempotency-key"
        );
    }

    private record TestGateway(
            TossPaymentGateway gateway,
            MockRestServiceServer server
    ) {
    }
}
