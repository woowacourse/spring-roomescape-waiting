package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
