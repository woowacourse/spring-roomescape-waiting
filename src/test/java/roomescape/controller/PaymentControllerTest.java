package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.global.auth.LoginMemberArgumentResolver;
import roomescape.global.config.WebConfig;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.service.AuthService;
import roomescape.service.PaymentService;

@WebMvcTest(PaymentController.class)
@Import({DomainErrorHttpMapper.class, LoginMemberArgumentResolver.class, WebConfig.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("사용자 결제 주문 내역 조회는 제공하지 않는다.")
    @Test
    void findOrdersNotSupported() throws Exception {
        mockMvc.perform(get("/payments/orders").session(loginSession()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    private MockHttpSession loginSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 1L);
        return session;
    }
}
