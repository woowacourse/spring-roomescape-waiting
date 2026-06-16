package roomescape.feature.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.feature.payment.dto.PaymentApproveRequest;

class PaymentApproverTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String APPROVE_URL = BASE_URL + "/v1/payments/confirm";

    private static final Long ORDER_ID = 1L;
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final Long AMOUNT = 1_000L;
    private static final PaymentApproveRequest APPROVE_REQUEST =
            new PaymentApproveRequest(ORDER_ID, PAYMENT_KEY, AMOUNT);

    private MockRestServiceServer mockTossServer;
    private PaymentApprover paymentApprover;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockTossServer = MockRestServiceServer.bindTo(builder).build();
        RestClient paymentRestClient = builder.build();

        paymentApprover = new PaymentApprover(paymentRestClient, new ObjectMapper());
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
