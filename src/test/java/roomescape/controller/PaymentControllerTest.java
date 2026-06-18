package roomescape.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.PaymentStatus;
import roomescape.dto.payment.PaymentResult;
import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.infrastructure.payment.TossPaymentException;
import roomescape.service.PaymentService;

@WebMvcTest(controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /payments/success - 결제 승인 결과를 응답한다")
    void successRespondsWithPaymentResult() throws Exception {
        given(paymentService.confirm("payment-key", "order-12345", 50000L))
                .willReturn(new PaymentResult("payment-key", "order-12345", PaymentStatus.DONE, 50000L));

        mockMvc.perform(post("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-12345")
                        .param("amount", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value("payment-key"))
                .andExpect(jsonPath("$.orderId").value("order-12345"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.approvedAmount").value(50000));
    }

    @Test
    @DisplayName("POST /payments/success - 금액이 일치하지 않으면 400과 메시지를 반환한다")
    void successReturns400WhenAmountMismatch() throws Exception {
        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willThrow(new RoomescapeException(ErrorType.PAYMENT_AMOUNT_MISMATCH, "결제 요청 금액이 주문 금액과 일치하지 않습니다."));

        mockMvc.perform(post("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-12345")
                        .param("amount", "9999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_AMOUNT_MISMATCH"));
    }

    @Test
    @DisplayName("POST /payments/success - 게이트웨이가 카드 거절 예외를 던지면 403과 코드를 반환한다")
    void successReturns403WhenCardRejected() throws Exception {
        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willThrow(new TossPaymentException.CardRejected("카드가 거절되었습니다."));

        mockMvc.perform(post("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-12345")
                        .param("amount", "50000"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("REJECT_CARD_PAYMENT"))
                .andExpect(jsonPath("$.message").value("카드가 거절되었습니다."));
    }

    @Test
    @DisplayName("POST /payments/success - 필수 파라미터가 누락되면 400과 메시지를 반환한다")
    void successReturns400WhenParamMissing() throws Exception {
        mockMvc.perform(post("/payments/success")
                        .param("orderId", "order-12345")
                        .param("amount", "50000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}