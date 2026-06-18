package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.PaymentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("내역 조회 시 name이 빈 문자열이면 400을 반환한다.")
    void getHistory_blankName_returns400() throws Exception {
        mockMvc.perform(get("/payment/history")
                        .param("name", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내역 조회 시 name 파라미터가 없으면 400을 반환한다.")
    void getHistory_missingName_returns400() throws Exception {
        mockMvc.perform(get("/payment/history"))
                .andExpect(status().isBadRequest());
    }
}
