package roomescape.reservation.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.UserReservationsResponse;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@WebMvcTest(value = ReservationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Nested
    @DisplayName("예약 조회 API")
    class GetReservations {
        @Test
        @DisplayName("전체 예약을 성공적으로 조회한다")
        void getReservationsSuccess() throws Exception {
            // given
            Member member = new Member(1L, "user@email.com", "password123", "홍길동", Role.USER);
            Theme theme = new Theme(1L, "방탈출 게임", "스릴 넘치는 방탈출", "thumbnail.jpg");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
            LocalDate date = LocalDate.now();

            List<ReservationResponse> responses = List.of(
                    new ReservationResponse(Reservation.createReserved(member, theme, date, time))
            );

            doReturn(responses).when(reservationService)
                    .getReservations(any(), any(), any(), any());

            // when && then
            mockMvc.perform(get("/reservations")
                            .param("memberId", "1")
                            .param("themeId", "1")
                            .param("dateFrom", date.toString())
                            .param("dateTo", date.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].member.id").value(1L))
                    .andExpect(jsonPath("$[0].theme.id").value(1L))
                    .andExpect(jsonPath("$[0].date").exists())
                    .andExpect(jsonPath("$[0].time.id").value(1L));
        }
    }

    @Nested
    @DisplayName("예약 생성 API")
    class CreateReservation {
        @Test
        @DisplayName("예약을 성공적으로 생성한다")
        void createReservationSuccess() throws Exception {
            // given
            Long memberId = 1L;
            ReservationRequest request = new ReservationRequest(
                    LocalDate.now(),
                    1L,
                    1L
            );

            Member member = new Member(memberId, "user@email.com", "password123", "홍길동", Role.USER);
            Theme theme = new Theme(1L, "방탈출 게임", "스릴 넘치는 방탈출", "thumbnail.jpg");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));

            ReservationResponse response = new ReservationResponse(
                    Reservation.createReserved(member, theme, request.getDate(), time)
            );

            doReturn(response).when(reservationService)
                    .createReservation(any(ReservationRequest.class), eq(memberId));

            // when & then
            mockMvc.perform(post("/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .param("memberId", String.valueOf(memberId)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.member.id").value(memberId))
                    .andExpect(jsonPath("$.theme.id").value(1L))
                    .andExpect(jsonPath("$.date").exists())
                    .andExpect(jsonPath("$.time.id").value(1L));
        }
    }

    @Nested
    @DisplayName("사용자 예약 조회 API")
    class GetUserReservations {
        @Test
        @DisplayName("사용자의 예약을 성공적으로 조회한다")
        void getUserReservationsSuccess() throws Exception {
            // given
            Long memberId = 1L;
            Member member = new Member(memberId, "user@email.com", "password123", "홍길동", Role.USER);
            Theme theme = new Theme(1L, "방탈출 게임", "스릴 넘치는 방탈출", "thumbnail.jpg");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
            LocalDate date = LocalDate.now();

            Reservation reservation = Reservation.createReserved(member, theme, date, time);
            ReservationWithRank reservationWithRank = new ReservationWithRank(reservation, 1L);

            List<UserReservationsResponse> responses = List.of(
                    new UserReservationsResponse(reservationWithRank)
            );

            doReturn(responses).when(reservationService)
                    .getUserReservations(memberId);

            // when && then
            mockMvc.perform(get("/reservations/mine")
                            .param("memberId", String.valueOf(memberId)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].theme").value("방탈출 게임"))
                    .andExpect(jsonPath("$[0].date").exists())
                    .andExpect(jsonPath("$[0].time").value("14:00:00"));
        }
    }

    @Nested
    @DisplayName("예약 삭제 API")
    class DeleteReservation {
        @Test
        @DisplayName("예약을 성공적으로 삭제한다")
        void deleteReservationSuccess() throws Exception {
            // given
            Long reservationId = 1L;
            Long memberId = 1L;

            // when && then
            mockMvc.perform(delete("/reservations/{id}", reservationId)
                            .param("memberId", String.valueOf(memberId)))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }
}
