package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import roomescape.dto.request.PaymentConfirmRequest;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.exception.GatewayTimeoutException;
import roomescape.exception.GlobalExceptionHandler;
import roomescape.payment.PaymentResult;
import roomescape.service.PaymentService;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean PaymentService paymentService;

    @Nested
    class POST_결제_승인 {

        @Test
        void 성공() throws Exception {
            PaymentConfirmRequest request = new PaymentConfirmRequest("pay-key-001", "order-001", 50000L);
            PaymentResult result = new PaymentResult("pay-key-001", "order-001", 50000L, "DONE");
            given(paymentService.confirm("pay-key-001", "order-001", 50000L)).willReturn(result);

            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentKey").value("pay-key-001"))
                    .andExpect(jsonPath("$.orderId").value("order-001"))
                    .andExpect(jsonPath("$.approvedAmount").value(50000))
                    .andExpect(jsonPath("$.status").value("DONE"));
        }

        @Test
        void 주문_없으면_404() throws Exception {
            PaymentConfirmRequest request = new PaymentConfirmRequest("pay-key-001", "order-001", 50000L);
            given(paymentService.confirm("pay-key-001", "order-001", 50000L))
                    .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 금액_불일치시_400() throws Exception {
            PaymentConfirmRequest request = new PaymentConfirmRequest("pay-key-001", "order-001", 99999L);
            given(paymentService.confirm("pay-key-001", "order-001", 99999L))
                    .willThrow(new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH));

            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 타임아웃_발생시_504() throws Exception {
            PaymentConfirmRequest request = new PaymentConfirmRequest("pay-key-001", "order-001", 50000L);
            given(paymentService.confirm("pay-key-001", "order-001", 50000L))
                    .willThrow(new GatewayTimeoutException(new ResourceAccessException("read timeout")));

            mockMvc.perform(post("/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isGatewayTimeout());
        }
    }
}
