package roomescape.admin.reservation.presentation;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.reservation.application.ReservationService;

@WebMvcTest(value = AdminReservationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
class AdminReservationWaitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Nested
    @DisplayName("대기 예약 승인 API")
    class AcceptWaitingReservation {
        @Test
        @DisplayName("관리자가 대기 예약을 성공적으로 승인한다")
        void acceptWaitingReservationSuccess() throws Exception {
            // given
            Long reservationId = 1L;

            doNothing().when(reservationService)
                    .acceptWaitingReservation(reservationId);

            // when && then
            mockMvc.perform(patch("/reservations/wait/accept/{reservationId}", reservationId))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("관리자가 아닌 사용자는 대기 예약을 승인할 수 없다")
        void acceptWaitingReservationFailUnauthorized() throws Exception {
            // given
            Long reservationId = 1L;

            // when && then
            mockMvc.perform(patch("/reservations/wait/accept/{reservationId}", reservationId))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 예약을 승인할 수 없다")
        void acceptNonExistentReservationFail() throws Exception {
            // given
            Long nonExistentReservationId = 999L;

            doThrow(new IllegalStateException("존재하지 않는 예약입니다."))
                    .when(reservationService)
                    .acceptWaitingReservation(nonExistentReservationId);

            // when && then
            mockMvc.perform(patch("/reservations/wait/accept/{reservationId}", nonExistentReservationId))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
