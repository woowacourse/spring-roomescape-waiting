package roomescape.admin.reservation.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.reservation.presentation.dto.AdminReservationRequest;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

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
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Nested
    @DisplayName("관리자 예약 생성 API")
    class CreateReservation {
        @Test
        @DisplayName("예약을 성공적으로 생성한다")
        void createReservationSuccess() throws Exception {
            // given
            AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);
            Member member = new Member(1L, "user@email.com", "password123", "홍길동", Role.USER);
            Theme theme = new Theme(1L, "방탈출 게임", "스릴 넘치는 방탈출", "thumbnail.jpg");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));

            ReservationResponse response = new ReservationResponse(
                    Reservation.createReserved(member, theme, LocalDate.now(), time)
            );

            doReturn(response).when(reservationService)
                    .createReservation(any(AdminReservationRequest.class));

            // when && then
            mockMvc.perform(post("/admin/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.date").exists())
                    .andExpect(jsonPath("$.member").exists())
                    .andExpect(jsonPath("$.theme").exists())
                    .andExpect(jsonPath("$.time").exists());
        }
    }

    @Nested
    @DisplayName("관리자 예약 삭제 API")
    class DeleteReservation {
        @Test
        @DisplayName("예약을 성공적으로 삭제한다")
        void deleteReservationSuccess() throws Exception {
            // given
            Long reservationId = 1L;

            // when && then
            mockMvc.perform(delete("/admin/reservations/{id}", reservationId))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }
}
