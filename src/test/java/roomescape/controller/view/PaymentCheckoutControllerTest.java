package roomescape.controller.view;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.service.PaymentService;

@WebMvcTest(value = PaymentCheckoutController.class, properties = "toss.client-key=test_gck_checkout")
class PaymentCheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void 결제_대기_정보로_checkout_화면을_렌더링한다() throws Exception {
        Payment payment = new Payment(1L, 2L, "payment_12345678901234567890123456789012", 20_000L, null,
                PaymentStatus.READY, null, null);
        given(paymentService.getReadyPayment(1L)).willReturn(payment);

        mockMvc.perform(get("/payments/{paymentId}/checkout", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attribute("clientKey", "test_gck_checkout"))
                .andExpect(model().attribute("paymentId", 1L))
                .andExpect(model().attribute("orderId", payment.getOrderId()))
                .andExpect(model().attribute("orderName", "방탈출 예약"))
                .andExpect(model().attribute("amount", 20_000L));
    }
}
