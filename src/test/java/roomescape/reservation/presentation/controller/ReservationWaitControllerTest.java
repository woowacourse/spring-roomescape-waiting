package roomescape.reservation.presentation.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@WebMvcTest(value = ReservationWaitController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
class ReservationWaitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Nested
    @DisplayName("대기 예약 생성 API")
    class CreateWaitingReservation {
        @Test
        @DisplayName("대기 예약을 성공적으로 생성한다")
        void createWaitingReservationSuccess() throws Exception {
            // given
            Long memberId = 1L;
            ReservationRequest request = new ReservationRequest(
                    LocalDate.of(2024, 5, 5),
                    1L,
                    1L
            );

            Member member = new Member(memberId, "user@email.com", "password123", "홍길동", Role.USER);
            Theme theme = new Theme(1L, "방탈출 게임", "스릴 넘치는 방탈출", "thumbnail.jpg");
            ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));

            ReservationResponse response = new ReservationResponse(
                    Reservation.createWaiting(member, theme, request.getDate(), time)
            );

            doReturn(response).when(reservationService)
                    .createWaitingReservation(any(ReservationRequest.class), eq(memberId));

            // when && then
            mockMvc.perform(post("/reservations/wait")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .param("memberId", String.valueOf(memberId)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            matchesPattern(".*/reservations/wait/")))
                    .andExpect(jsonPath("$.id").value(response.getId()))
                    .andExpect(jsonPath("$.member.id").value(memberId))
                    .andExpect(jsonPath("$.theme.id").value(1L))
                    .andExpect(jsonPath("$.date").value("2024-05-05"))
                    .andExpect(jsonPath("$.time.id").value(1L));
        }

        @Test
        @DisplayName("유효하지 않은 요청으로 대기 예약을 생성할 수 없다")
        void createWaitingReservationFailInvalidRequest() throws Exception {
            // given
            ReservationRequest invalidRequest = new ReservationRequest(
                    null,
                    null,
                    null
            );

            // when && then
            mockMvc.perform(post("/reservations/wait")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이미 예약된 시간에는 대기 예약을 생성할 수 없다")
        void createWaitingReservationFailAlreadyBooked() throws Exception {
            // given
            Long memberId = 1L;
            ReservationRequest request = new ReservationRequest(
                    LocalDate.of(2024, 5, 5),
                    1L,
                    1L
            );

            doThrow(new IllegalStateException("이미 예약된 시간입니다."))
                    .when(reservationService)
                    .createWaitingReservation(any(ReservationRequest.class), eq(memberId));

            // when & then
            mockMvc.perform(post("/reservations/wait")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
