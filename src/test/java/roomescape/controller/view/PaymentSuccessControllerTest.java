package roomescape.controller.view;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.PaymentStatus;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentFailureCategory;
import roomescape.payment.PaymentGatewayException;
import roomescape.payment.PaymentResult;
import roomescape.service.PaymentService;

@WebMvcTest(PaymentSuccessController.class)
class PaymentSuccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void 결제_승인_결과를_성공_화면으로_렌더링한다() throws Exception {
        PaymentResult result = new PaymentResult("test_payment_key", "payment_123456789012345678901",
                PaymentStatus.CONFIRMED, 20_000L);
        given(paymentService.confirm("test_payment_key", result.orderId(), 20_000L)).willReturn(result);

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "test_payment_key")
                        .param("orderId", result.orderId())
                        .param("amount", "20000"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-success"))
                .andExpect(model().attribute("result", result));
    }

    @Test
    void 결제_금액이_다르면_실패_화면으로_렌더링한다() throws Exception {
        String orderId = "payment_123456789012345678901";
        given(paymentService.confirm("test_payment_key", orderId, 19_000L))
                .willThrow(new PaymentAmountMismatchException(20_000L, 19_000L));

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "test_payment_key")
                        .param("orderId", orderId)
                        .param("amount", "19000"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-fail"))
                .andExpect(model().attribute("code", "AMOUNT_MISMATCH"))
                .andExpect(model().attribute("orderId", orderId));
    }

    @Test
    void 결제_승인_오류를_실패_화면으로_렌더링한다() throws Exception {
        String orderId = "payment_123456789012345678901";
        given(paymentService.confirm("test_payment_key", orderId, 20_000L))
                .willThrow(new PaymentGatewayException(
                        PaymentFailureCategory.DEFINITIVE, "REJECT_CARD_PAYMENT", "카드가 거절되었습니다."));

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "test_payment_key")
                        .param("orderId", orderId)
                        .param("amount", "20000"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-fail"))
                .andExpect(model().attribute("code", "REJECT_CARD_PAYMENT"))
                .andExpect(model().attribute("orderId", orderId));
    }

    @Test
    void 결제_실패_콜백은_실패_정보를_저장하고_실패_화면을_렌더링한다() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("paymentId", "1")
                        .param("code", "REJECT_CARD_PAYMENT")
                        .param("message", "카드가 거절되었습니다.")
                        .param("orderId", "payment_123456789012345678901"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-fail"))
                .andExpect(model().attribute("code", "REJECT_CARD_PAYMENT"));

        verify(paymentService).fail(1L, "REJECT_CARD_PAYMENT", "카드가 거절되었습니다.");
    }

    @Test
    void 결제_취소는_orderId가_없어도_실패_화면을_렌더링한다() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "PAY_PROCESS_CANCELED")
                        .param("message", "사용자가 결제를 취소했습니다."))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-fail"))
                .andExpect(model().attribute("code", "PAY_PROCESS_CANCELED"))
                .andExpect(model().attribute("orderId", (Object) null));
    }
}
