package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.config.WebConfig;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.payment.PaymentOrderStatus;
import roomescape.service.AuthService;
import roomescape.service.PaymentService;
import roomescape.service.dto.PaymentOrderHistory;

@WebMvcTest(AdminPaymentController.class)
@Import({DomainErrorHttpMapper.class, AdminAuthorizationInterceptor.class, WebConfig.class})
class AdminPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("관리자는 전체 결제 주문 내역을 조회한다.")
    @Test
    void findOrders() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());
        given(paymentService.findAllOrders()).willReturn(List.of(
                new PaymentOrderHistory(
                        "order-123456",
                        10L,
                        PaymentOrderStatus.CONFIRMED,
                        "payment-key",
                        23000,
                        null,
                        null,
                        LocalDate.of(2026, 7, 1),
                        LocalTime.of(10, 0),
                        "잠긴 방",
                        "닫힌 문을 여는 테마",
                        "https://example.com/theme.jpg"
                )
        ));

        mockMvc.perform(get("/admin/payments/orders").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("order-123456"))
                .andExpect(jsonPath("$[0].reservationId").value(10))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].statusLabel").value("예약 확정"))
                .andExpect(jsonPath("$[0].paymentKey").value("payment-key"))
                .andExpect(jsonPath("$[0].amount").value(23000))
                .andExpect(jsonPath("$[0].themeName").value("잠긴 방"));
    }

    @DisplayName("일반 사용자는 전체 결제 주문 내역을 조회할 수 없다.")
    @Test
    void findOrdersUnauthorized() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 1L);
        given(authService.getLoginMember(1L))
                .willReturn(new Member(1L, "user", "사용자", "password", Role.USER));

        mockMvc.perform(get("/admin/payments/orders").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_ADMIN"));
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 7L);
        return session;
    }

    private Member admin() {
        return new Member(7L, "admin", "관리자", "password", Role.ADMIN);
    }
}
