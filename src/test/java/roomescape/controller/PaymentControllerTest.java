package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.PreparePaymentRequest;
import roomescape.exception.ProblemDetailsAdvice;
import roomescape.service.SessionService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(ProblemDetailsAdvice.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @Test
    @DisplayName("결제 준비 요청 시 서버 생성 orderId를 반환하고 200을 응답한다.")
    void prepare() throws Exception {
        given(sessionService.preparePayment(50000L)).willReturn("test-order-uuid");

        mockMvc.perform(post("/payments/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PreparePaymentRequest(50000L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("test-order-uuid"));
    }

    @Test
    @DisplayName("amount 누락 시 400과 INVALID_REQUEST_BODY를 반환한다.")
    void prepareWithMissingAmount() throws Exception {
        mockMvc.perform(post("/payments/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    @DisplayName("결제 취소 요청 시 204를 응답한다.")
    void cancel() throws Exception {
        doNothing().when(sessionService).cancelPreparedPayment("test-order-uuid");

        mockMvc.perform(delete("/payments/prepare/test-order-uuid"))
                .andExpect(status().isNoContent());
    }
}
