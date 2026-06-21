package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.service.PaymentService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentCallbackController.class)
class PaymentCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void 결제_성공_콜백은_결제를_승인하고_성공_결과로_리다이렉트한다() throws Exception {
        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-1")
                        .param("amount", "10000"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/reservation.html?payment=success&code=DONE&message=*&orderId=order-1&amount=10000"));

        verify(paymentService).confirm("payment-key", "order-1", 10_000L);
    }

    @Test
    void 결제_승인_중_금액이_다르면_실패_결과로_리다이렉트한다() throws Exception {
        willThrow(new RoomEscapeException(PaymentErrorCode.AMOUNT_MISMATCH))
                .given(paymentService)
                .confirm(anyString(), anyString(), anyLong());

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-1")
                        .param("amount", "9000"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/reservation.html?payment=fail&code=AMOUNT_MISMATCH&message=*&orderId=order-1&amount=9000"));
    }

    @Test
    void 결제_실패_콜백은_주문을_정리하고_실패_결과로_리다이렉트한다() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "PAY_PROCESS_CANCELED")
                        .param("message", "사용자가 결제를 취소했습니다.")
                        .param("orderId", "order-1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/reservation.html?payment=fail&code=PAY_PROCESS_CANCELED&message=*&orderId=order-1"));

        verify(paymentService).fail("order-1");
    }

    @Test
    void orderId가_없는_결제_실패_콜백도_처리한다() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "PAY_PROCESS_CANCELED")
                        .param("message", "사용자가 결제를 취소했습니다."))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("orderId="))))
                .andExpect(redirectedUrlPattern("/reservation.html?payment=fail&code=PAY_PROCESS_CANCELED&message=*"));

        verify(paymentService).fail(null);
    }
}
