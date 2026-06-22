package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.PaymentService;
import roomescape.exception.client.PaymentRejectedException;
import roomescape.exception.server.PaymentTimeoutException;

/**
 * PaymentController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>여기서만 검증할 고유 책임: 승인 콜백에서 "예외 종류 → 어떤 정리 동작 + 어떤 결과 리다이렉트"로 가는 분기.
 * 실제 IN_DOUBT 커밋/예약 보존은 인수 테스트가, markInDoubt 자체의 가드는 PaymentServiceTest가 책임진다.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void 승인_성공이면_success로_리다이렉트한다() throws Exception {
        doNothing().when(paymentService).confirm(anyString(), anyString(), anyLong());

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "pk").param("orderId", "order-1").param("amount", "1000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment-result.html?status=success*"));
    }

    @Test
    void 타임아웃이면_삭제하지_않고_확인필요로_표시하며_pending으로_리다이렉트한다() throws Exception {
        doThrow(new PaymentTimeoutException("확인 필요"))
                .when(paymentService).confirm(anyString(), anyString(), anyLong());

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "pk").param("orderId", "order-1").param("amount", "1000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment-result.html?status=pending*"));

        verify(paymentService).markInDoubt("order-1");
        verify(paymentService, never()).cancelPending(anyString());   // 절대 삭제 안 함
    }

    @Test
    void 거절이면_대기_주문을_정리하고_fail로_리다이렉트한다() throws Exception {
        doThrow(new PaymentRejectedException("카드 거절"))
                .when(paymentService).confirm(anyString(), anyString(), anyLong());

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "pk").param("orderId", "order-1").param("amount", "1000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment-result.html?status=fail*"));

        verify(paymentService).cancelPending("order-1");
        verify(paymentService, never()).markInDoubt(anyString());
    }
}
