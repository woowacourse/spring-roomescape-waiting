package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.infrastructure.AdminAuthorizationInterceptor;
import roomescape.infrastructure.LoginCheckInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.PaymentService;

@WebMvcTest(controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, LoginCheckInterceptor.class,
                        AdminAuthorizationInterceptor.class, LoginUserArgumentResolver.class}))
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void GET_payments_success_결제_승인_결과를_응답한다() throws Exception {
        given(paymentService.confirmPayment("payment_key", "order_123456", 37_000L))
                .willReturn(new PaymentResult("payment_key", "order_123456", 37_000L));

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment_key")
                        .param("orderId", "order_123456")
                        .param("amount", "37000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value("payment_key"))
                .andExpect(jsonPath("$.orderId").value("order_123456"))
                .andExpect(jsonPath("$.amount").value(37_000));
    }

    @Test
    void GET_payments_success_금액이_다르면_409를_응답한다() throws Exception {
        given(paymentService.confirmPayment("payment_key", "order_123456", 1_000L))
                .willThrow(new PaymentAmountMismatchException());

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment_key")
                        .param("orderId", "order_123456")
                        .param("amount", "1000"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("결제 금액이 주문 금액과 일치하지 않습니다."));
    }

    @Test
    void GET_payments_fail_결제_대기_주문을_정리하고_실패_페이지로_이동한다() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "REJECT_CARD_PAYMENT")
                        .param("message", "카드 결제가 거절되었습니다.")
                        .param("orderId", "order_123456"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "/payment-fail.html?code=REJECT_CARD_PAYMENT&message=%EC%B9%B4%EB%93%9C%20%EA%B2%B0%EC%A0%9C%EA%B0%80%20%EA%B1%B0%EC%A0%88%EB%90%98%EC%97%88%EC%8A%B5%EB%8B%88%EB%8B%A4.&orderId=order_123456"));

        verify(paymentService).failPayment("order_123456");
    }

    @Test
    void GET_payments_fail_orderId가_없어도_실패_페이지로_이동한다() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "PAY_PROCESS_CANCELED")
                        .param("message", "사용자가 결제를 취소했습니다."))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "/payment-fail.html?code=PAY_PROCESS_CANCELED&message=%EC%82%AC%EC%9A%A9%EC%9E%90%EA%B0%80%20%EA%B2%B0%EC%A0%9C%EB%A5%BC%20%EC%B7%A8%EC%86%8C%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4."));

        verify(paymentService).failPayment(null);
    }
}
