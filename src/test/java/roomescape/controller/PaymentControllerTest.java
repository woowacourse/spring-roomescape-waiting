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
import roomescape.global.auth.LoginMemberArgumentResolver;
import roomescape.global.config.WebConfig;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.payment.PaymentOrderStatus;
import roomescape.service.AuthService;
import roomescape.service.PaymentService;
import roomescape.service.dto.PaymentOrderHistory;

@WebMvcTest(PaymentController.class)
@Import({DomainErrorHttpMapper.class, LoginMemberArgumentResolver.class, WebConfig.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("로그인 사용자의 결제 주문 내역을 조회한다.")
    @Test
    void findOrders() throws Exception {
        Member member = new Member(1L, "roro", "러로", "password", Role.USER);
        given(authService.getLoginMember(1L)).willReturn(member);
        given(paymentService.findOrdersByMember(member)).willReturn(List.of(
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
                ),
                new PaymentOrderHistory(
                        "order-unknown",
                        null,
                        PaymentOrderStatus.CONFIRM_UNKNOWN,
                        null,
                        20000,
                        "PAYMENT_CONFIRM_UNKNOWN",
                        "결제 승인 응답을 받지 못했습니다.",
                        LocalDate.of(2026, 7, 2),
                        LocalTime.of(11, 0),
                        "우주선",
                        "탈출 테마",
                        "https://example.com/space.jpg"
                )
        ));

        mockMvc.perform(get("/payments/orders").session(loginSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("order-123456"))
                .andExpect(jsonPath("$[0].reservationId").value(10))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].statusLabel").value("예약 확정"))
                .andExpect(jsonPath("$[0].paymentKey").value("payment-key"))
                .andExpect(jsonPath("$[0].amount").value(23000))
                .andExpect(jsonPath("$[0].themeName").value("잠긴 방"))
                .andExpect(jsonPath("$[1].orderId").value("order-unknown"))
                .andExpect(jsonPath("$[1].status").value("CONFIRM_UNKNOWN"))
                .andExpect(jsonPath("$[1].statusLabel").value("확인 필요"))
                .andExpect(jsonPath("$[1].failureCode").value("PAYMENT_CONFIRM_UNKNOWN"));
    }

    private MockHttpSession loginSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 1L);
        return session;
    }
}
