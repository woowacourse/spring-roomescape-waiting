package roomescape.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.infra.toss.TossPaymentException;
import roomescape.service.PaymentService;

@WebMvcTest(PaymentViewController.class)
@TestPropertySource(properties = "toss.client-key=test_client_key")
class PaymentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 화면에 위젯 렌더링에 필요한 값을 전달한다.")
    void 결제_화면() throws Exception {
        mockMvc.perform(get("/checkout")
                        .param("orderId", "order-1")
                        .param("amount", "50000")
                        .param("orderName", "방탈출 예약"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attribute("clientKey", "test_client_key"))
                .andExpect(model().attribute("orderId", "order-1"))
                .andExpect(model().attribute("amount", 50000L))
                .andExpect(model().attribute("orderName", "방탈출 예약"));
    }

    @Test
    @DisplayName("결제 승인에 성공하면 성공 화면을 반환한다.")
    void 결제_성공() throws Exception {
        PaymentResult result = new PaymentResult("payment-key", "order-1", PaymentStatus.DONE, 50000L);
        given(paymentService.confirm("payment-key", "order-1", 50000L))
                .willReturn(result);

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-1")
                        .param("amount", "50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("success"))
                .andExpect(model().attribute("result", result))
                .andExpect(model().attributeDoesNotExist("paymentKey"));
    }

    @Test
    @DisplayName("결제 금액이 일치하지 않으면 실패 화면을 반환한다.")
    void 결제_금액_불일치() throws Exception {
        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willThrow(new PaymentAmountMismatchException(50000L, 10000L));

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-1")
                        .param("amount", "10000"))
                .andExpect(status().isOk())
                .andExpect(view().name("fail"))
                .andExpect(model().attribute("code", "AMOUNT_MISMATCH"))
                .andExpect(model().attribute("orderId", "order-1"));
    }

    @Test
    @DisplayName("Toss 결제 승인에 실패하면 실패 화면을 반환한다.")
    void 토스_결제_실패() throws Exception {
        given(paymentService.confirm(anyString(), anyString(), anyLong()))
                .willThrow(new TossPaymentException(org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."));

        mockMvc.perform(get("/payments/success")
                        .param("paymentKey", "payment-key")
                        .param("orderId", "order-1")
                        .param("amount", "50000"))
                .andExpect(status().isOk())
                .andExpect(view().name("fail"))
                .andExpect(model().attribute("code", "INVALID_REQUEST"))
                .andExpect(model().attribute("message", "잘못된 요청입니다."))
                .andExpect(model().attribute("orderId", "order-1"));
    }

    @Test
    @DisplayName("결제 인증 실패 콜백은 실패 화면을 반환한다.")
    void 결제_인증_실패() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "USER_CANCEL")
                        .param("message", "사용자가 결제를 취소했습니다.")
                        .param("orderId", "order-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fail"))
                .andExpect(model().attribute("code", "USER_CANCEL"))
                .andExpect(model().attribute("message", "사용자가 결제를 취소했습니다."))
                .andExpect(model().attribute("orderId", "order-1"));
    }

    @Test
    @DisplayName("결제 인증 실패 콜백에 주문번호가 없어도 실패 화면을 반환한다.")
    void 결제_인증_실패_주문번호_없음() throws Exception {
        mockMvc.perform(get("/payments/fail")
                        .param("code", "USER_CANCEL")
                        .param("message", "사용자가 결제를 취소했습니다."))
                .andExpect(status().isOk())
                .andExpect(view().name("fail"))
                .andExpect(model().attribute("code", "USER_CANCEL"))
                .andExpect(model().attribute("message", "사용자가 결제를 취소했습니다."))
                .andExpect(model().attribute("orderId", nullValue()));
    }
}
