package roomescape.reservation.time.presentation;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.reservation.time.application.ReservationTimeService;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;

@WebMvcTest(value = ReservationTimeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Nested
    @DisplayName("예약 시간 조회 API")
    class GetReservationTimes {
        @Test
        @DisplayName("전체 예약 시간을 조회한다")
        void getReservationTimesSuccess() throws Exception {
            // given
            List<ReservationTimeResponse> responses = List.of(
                    new ReservationTimeResponse(new ReservationTime(1L, LocalTime.of(10, 0))),
                    new ReservationTimeResponse(new ReservationTime(2L, LocalTime.of(12, 0)))
            );

            doReturn(responses).when(reservationTimeService)
                    .getReservationTimes();

            // when & then
            mockMvc.perform(get("/times"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].startAt").value("10:00"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].startAt").value("12:00"));
        }
    }

    @Nested
    @DisplayName("사용 가능한 예약 시간 조회 API")
    class GetAvailableReservationTimes {
        @Test
        @DisplayName("특정 날짜와 테마에 대해 사용 가능한 예약 시간을 조회한다")
        void getAvailableReservationTimesSuccess() throws Exception {
            // given
            LocalDate date = LocalDate.of(2024, 5, 5);
            Long themeId = 1L;

            List<AvailableReservationTimeResponse> responses = List.of(
                    new AvailableReservationTimeResponse(
                            new ReservationTime(1L, LocalTime.of(10, 0)), false),
                    new AvailableReservationTimeResponse(
                            new ReservationTime(2L, LocalTime.of(12, 0)), true)
            );

            doReturn(responses).when(reservationTimeService)
                    .getReservationTimes(date, themeId);

            // when & then
            mockMvc.perform(get("/times/available")
                            .param("date", date.toString())
                            .param("themeId", themeId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].startAt").value("10:00"))
                    .andExpect(jsonPath("$[0].alreadyBooked").value(false))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].startAt").value("12:00"))
                    .andExpect(jsonPath("$[1].alreadyBooked").value(true));
        }

        @Test
        @DisplayName("날짜 파라미터가 없으면 400 응답을 반환한다")
        void getAvailableReservationTimesFailWithoutDate() throws Exception {
            // given
            Long themeId = 1L;

            // when & then
            mockMvc.perform(get("/times/available")
                            .param("themeId", themeId.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("테마 ID 파라미터가 없으면 400 응답을 반환한다")
        void getAvailableReservationTimesFailWithoutThemeId() throws Exception {
            // given
            LocalDate date = LocalDate.of(2024, 5, 5);

            // when & then
            mockMvc.perform(get("/times/available")
                            .param("date", date.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
