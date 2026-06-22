package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentConfigController.class)
@TestPropertySource(properties = "toss.client-key=test_ck_dummy")
class PaymentConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 결제_클라이언트_키를_조회한다() throws Exception {
        mockMvc.perform(get("/payments/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientKey").value("test_ck_dummy"));
    }
}
