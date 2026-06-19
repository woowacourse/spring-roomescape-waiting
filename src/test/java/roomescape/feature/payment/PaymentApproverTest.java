package roomescape.feature.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.feature.payment.config.TossPaymentProperties;
import roomescape.feature.payment.dto.PaymentApproveRequest;

class PaymentApproverTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String APPROVE_URL = BASE_URL + "/v1/payments/confirm";

    private static final String ORDER_ID = "test_order_id";
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final Long AMOUNT = 1_000L;
    private static final PaymentApproveRequest APPROVE_REQUEST =
            new PaymentApproveRequest(ORDER_ID, PAYMENT_KEY, AMOUNT);

    private MockRestServiceServer mockTossServer;
    private PaymentApprover paymentApprover;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockTossServer = MockRestServiceServer.bindTo(builder).build();

        TossPaymentProperties properties = new TossPaymentProperties(
                BASE_URL, "test_secret_key", Duration.ofSeconds(5), Duration.ofSeconds(10));
        TossPaymentClient tossPaymentClient = new TossPaymentClient(builder, properties, new ObjectMapper());

        paymentApprover = new PaymentApprover(tossPaymentClient);
    }

    @Test
    void 승인에_성공하면_true를_반환한다() {
        // given
        mockTossServer.expect(requestTo(APPROVE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentKey").value(PAYMENT_KEY))
                .andRespond(withSuccess(
                        "{\"status\":\"DONE\",\"paymentKey\":\"%s\"}".formatted(PAYMENT_KEY),
                        MediaType.APPLICATION_JSON));

        // when
        boolean approved = paymentApprover.approve(APPROVE_REQUEST);

        // then
        assertThat(approved).isTrue();
        mockTossServer.verify();
    }

    @Test
    void 승인_응답_상태가_DONE이_아니면_false를_반환한다() {
        // given: 가상계좌처럼 승인 즉시 완료되지 않는 상태로 응답
        mockTossServer.expect(requestTo(APPROVE_URL))
                .andRespond(withSuccess(
                        "{\"status\":\"WAITING_FOR_DEPOSIT\",\"paymentKey\":\"%s\"}".formatted(PAYMENT_KEY),
                        MediaType.APPLICATION_JSON));

        // when
        boolean approved = paymentApprover.approve(APPROVE_REQUEST);

        // then
        assertThat(approved).isFalse();
    }

    @Test
    void 멱등성을_위해_Idempotency_Key_헤더로_orderId를_전송한다() {
        // given
        mockTossServer.expect(requestTo(APPROVE_URL))
                .andExpect(header("Idempotency-Key", ORDER_ID))
                .andRespond(withSuccess(
                        "{\"status\":\"DONE\",\"paymentKey\":\"%s\"}".formatted(PAYMENT_KEY),
                        MediaType.APPLICATION_JSON));

        // when
        boolean approved = paymentApprover.approve(APPROVE_REQUEST);

        // then
        assertThat(approved).isTrue();
        mockTossServer.verify();
    }

    @Test
    void 연결_실패는_PaymentConnectionException으로_변환한다() {
        // given: TCP 연결 수립 실패 (ConnectException) → ResourceAccessException 으로 표면화
        mockTossServer.expect(requestTo(APPROVE_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("connection refused", new ConnectException("Connection refused"));
                });

        // when
        Throwable thrown = catchThrowable(() -> paymentApprover.approve(APPROVE_REQUEST));

        // then
        assertThat(thrown).isInstanceOf(PaymentConnectionException.class);
    }

    @Test
    void 느린_응답은_PaymentTimeoutException으로_변환한다() {
        // given: 읽기 타임아웃 (SocketTimeoutException) → ResourceAccessException 으로 표면화
        mockTossServer.expect(requestTo(APPROVE_URL))
                .andRespond(request -> {
                    throw new ResourceAccessException("read timed out", new SocketTimeoutException("Read timed out"));
                });

        // when
        Throwable thrown = catchThrowable(() -> paymentApprover.approve(APPROVE_REQUEST));

        // then
        assertThat(thrown).isInstanceOf(PaymentTimeoutException.class);
    }

    @Test
    void 이미_처리된_결제는_승인_성공으로_간주해_true를_반환한다() {
        // given: 토스가 ALREADY_PROCESSED_PAYMENT 에러를 응답 (멱등 케이스)
        mockTossServer.expect(requestTo(APPROVE_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"ALREADY_PROCESSED_PAYMENT\",\"message\":\"이미 처리된 결제 입니다.\"}"));

        // when
        boolean approved = paymentApprover.approve(APPROVE_REQUEST);

        // then
        assertThat(approved).isTrue();
    }

    @Nested
    class 승인_API가_에러를_응답하면 {

        @Test
        void 매핑된_에러코드는_해당_실패유형의_PaymentException으로_변환한다() {
            // given
            String rejectCode = "REJECT_CARD_COMPANY";
            String rejectMessage = "결제 승인이 거절되었습니다.";
            mockTossServer.expect(requestTo(APPROVE_URL))
                    .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"code\":\"%s\",\"message\":\"%s\"}".formatted(rejectCode, rejectMessage)));

            // when
            Throwable thrown = catchThrowable(() -> paymentApprover.approve(APPROVE_REQUEST));

            // then
            assertThat(thrown)
                    .isInstanceOf(PaymentException.class)
                    .hasMessage(rejectMessage);

            PaymentException exception = (PaymentException) thrown;
            assertThat(exception.getCode()).isEqualTo(rejectCode);
            assertThat(exception.getFailureType()).isEqualTo(PaymentFailureType.CARD_DECLINED);
        }

        @Test
        void 매핑되지_않은_에러코드는_UNKNOWN_유형으로_변환한다() {
            // given
            String unmappedCode = "NOT_FOUND_PAYMENT";
            String notFoundMessage = "존재하지 않는 결제 입니다.";
            mockTossServer.expect(requestTo(APPROVE_URL))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"code\":\"%s\",\"message\":\"%s\"}".formatted(unmappedCode, notFoundMessage)));

            // when
            Throwable thrown = catchThrowable(() -> paymentApprover.approve(APPROVE_REQUEST));

            // then
            assertThat(thrown)
                    .isInstanceOf(PaymentException.class)
                    .hasMessage(notFoundMessage);

            PaymentException exception = (PaymentException) thrown;
            assertThat(exception.getCode()).isEqualTo(unmappedCode);
            assertThat(exception.getFailureType()).isEqualTo(PaymentFailureType.UNKNOWN);
        }
    }
}
