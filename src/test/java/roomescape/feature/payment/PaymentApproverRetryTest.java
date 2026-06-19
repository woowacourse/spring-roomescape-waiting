package roomescape.feature.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.feature.payment.dto.PaymentApproveRequest;

/**
 * 전송 계층 실패의 재시도 + recover 분기를 Spring 프록시(@Retryable)가 적용된 상태로 검증한다.
 * MockRestServiceServer 로 토스 응답을 대체하므로 실제 네트워크에는 의존하지 않는다.
 *
 * - 연결 실패: 재시도 소진 후 PaymentConnectionException 그대로 전파 (→ 503)
 * - 읽기 타임아웃: 재시도 소진 후 상태 조회(reconciliation) → DONE 이면 성공, 아니면 PaymentTimeoutException 전파 (→ 504)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentApproverRetryTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String CONFIRM_URL = BASE_URL + "/v1/payments/confirm";
    private static final String ORDER_ID = "test_order_id";
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final String STATUS_URL = BASE_URL + "/v1/payments/" + PAYMENT_KEY;
    private static final PaymentApproveRequest REQUEST =
            new PaymentApproveRequest(ORDER_ID, PAYMENT_KEY, 1_000L);
    private static final int MAX_ATTEMPTS = 3;

    @Autowired
    private PaymentApprover paymentApprover;

    @Autowired
    private MockTossServerConfig mockTossServerConfig;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = mockTossServerConfig.server;
        mockServer.reset();
    }

    @Test
    void 연결_실패는_재시도를_소진한_뒤_PaymentConnectionException을_던진다() {
        // given: 매 시도마다 연결 실패
        mockServer.expect(times(MAX_ATTEMPTS), requestTo(CONFIRM_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new ResourceAccessException("connection refused", new ConnectException("Connection refused"));
                });

        // when
        Throwable thrown = catchThrowable(() -> paymentApprover.approve(REQUEST));

        // then: 미청구 확정 → 그대로 전파 (상태 조회 없음)
        assertThat(thrown).isInstanceOf(PaymentConnectionException.class);
        mockServer.verify();
    }

    @Test
    void 읽기_타임아웃_재시도_소진_후_상태조회가_DONE이면_true를_반환한다() {
        // given: 매 시도마다 읽기 타임아웃 → 소진 후 상태 조회 시 DONE
        mockServer.expect(times(MAX_ATTEMPTS), requestTo(CONFIRM_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("read timed out", new SocketTimeoutException("Read timed out"));
                });
        mockServer.expect(requestTo(STATUS_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"status\":\"DONE\",\"paymentKey\":\"%s\"}".formatted(PAYMENT_KEY),
                        MediaType.APPLICATION_JSON));

        // when
        boolean approved = paymentApprover.approve(REQUEST);

        // then: 실제로는 승인 완료였음 → 예약 확정으로 진행
        assertThat(approved).isTrue();
        mockServer.verify();
    }

    @Test
    void 읽기_타임아웃_재시도_소진_후에도_DONE이_아니면_PaymentTimeoutException을_던진다() {
        // given: 매 시도마다 읽기 타임아웃 → 소진 후 상태 조회해도 아직 미완료
        mockServer.expect(times(MAX_ATTEMPTS), requestTo(CONFIRM_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("read timed out", new SocketTimeoutException("Read timed out"));
                });
        mockServer.expect(requestTo(STATUS_URL))
                .andRespond(withSuccess(
                        "{\"status\":\"IN_PROGRESS\",\"paymentKey\":\"%s\"}".formatted(PAYMENT_KEY),
                        MediaType.APPLICATION_JSON));

        // when
        Throwable thrown = catchThrowable(() -> paymentApprover.approve(REQUEST));

        // then: 결과 불명 → 실패로 단정하지 않고 '확인 중'으로 표면화
        assertThat(thrown).isInstanceOf(PaymentTimeoutException.class);
        mockServer.verify();
    }

    @Test
    void 비즈니스_에러는_재시도하지_않고_PaymentException을_그대로_전파한다() {
        // given: 토스가 카드 거절 에러를 응답 (재시도 대상이 아닌 비즈니스 결과)
        mockServer.expect(times(1), requestTo(CONFIRM_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"REJECT_CARD_COMPANY\",\"message\":\"카드사에서 결제를 거절했습니다.\"}"));

        // when
        Throwable thrown = catchThrowable(() -> paymentApprover.approve(REQUEST));

        // then: ExhaustedRetryException 으로 감싸지지 않고 원래 PaymentException 이 1회 호출로 전파된다
        assertThat(thrown).isInstanceOf(PaymentException.class);
        mockServer.verify();
    }

    @TestConfiguration
    static class MockTossServerConfig {

        private MockRestServiceServer server;

        @Bean
        @Primary
        RestClient.Builder mockTossRestClientBuilder() {
            RestClient.Builder builder = RestClient.builder();
            this.server = MockRestServiceServer.bindTo(builder)
                    .ignoreExpectOrder(true)
                    .build();
            return builder;
        }
    }
}
