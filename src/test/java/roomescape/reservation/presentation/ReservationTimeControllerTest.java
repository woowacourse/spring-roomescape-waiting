package roomescape.reservation.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import roomescape.auth.presentation.AdminAuthorizationInterceptor;
import roomescape.auth.presentation.LoginMemberArgumentResolver;
import roomescape.common.ControllerTest;
import roomescape.global.config.WebMvcConfiguration;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;

@WebMvcTest(
        value = ReservationTimeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class ReservationTimeControllerTest extends ControllerTest {
    @MockBean
    protected ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간 POST 요청 시 상태코드 201을 반환한다.")
    void createReservationTime() throws Exception {
        // given
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(MIA_RESERVATION_TIME);
        ReservationTime expectedReservationTime = new ReservationTime(1L, request.toModel());

        BDDMockito.given(reservationTimeService.create(any()))
                .willReturn(expectedReservationTime);

        // when & then
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.startAt").value(MIA_RESERVATION_TIME.toString()));
    }

    @Test
    @DisplayName("예약 시간 POST 요청 시 10분 단위가 아닐 경우 상태코드 400을 반환한다.")
    void createReservationTimeWithInvalidTimeUnit() throws Exception {
        // given
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(15, 3));

        // when & then
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예약 시간 POST 요청 시 필드가 없다면 상태코드 400을 반환한다.")
    void createReservationTimeWithNullFieldRequest() throws Exception {
        // given
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(null);

        // when & then
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예약 시간 POST 요청 시 시간 형식이 올바르지 않을 경우 상태코드 400을 반환한다.")
    void createReservationTimeWithInvalidFormat() throws Exception {
        // given
        String invalidFormatRequest = "{\"startAt\": \"invalid-time\"}";

        // when & then
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidFormatRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예약 시간 목록 GET 요청 시 상태코드 200을 반환한다.")
    void findReservationTimes() throws Exception {
        // given
        BDDMockito.given(reservationTimeService.findAll())
                .willReturn(List.of(new ReservationTime(MIA_RESERVATION_TIME)));

        // when & then
        mockMvc.perform(get("/times").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startAt").value(MIA_RESERVATION_TIME.toString()));
    }

    @Test
    @DisplayName("예약 시간 DELETE 요청 시 상태코드 204를 반환한다.")
    void deleteReservationTime() throws Exception {
        // given
        BDDMockito.willDoNothing()
                .given(reservationTimeService)
                .delete(anyLong());

        // when & then
        mockMvc.perform(delete("/times/{id}", anyLong())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("에약 가능 시간 목록 GET 요청 시 상태 코드를 200을 반환한다.")
    void findAllByDateAndThemeId() throws Exception {
        // given
        long themeId = 1L;
        BDDMockito.given(reservationTimeService.findAvailableReservationTimes(MIA_RESERVATION_DATE, themeId))
                .willReturn(List.of(AvailableReservationTimeResponse.of(new ReservationTime(MIA_RESERVATION_TIME), true)));

        // when & then
        mockMvc.perform(get("/times/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("date", MIA_RESERVATION_DATE.toString())
                        .param("themeId", Long.toString(themeId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$[0].isReserved").value(true));
    }

    @Test
    @DisplayName("올바르지 않은 예약 날짜 형식의 쿼리 스트링으로 예약 가능 시간 목록 GET 요청 시 상태 코드를 400을 반환한다.")
    void findAllByDateAndThemeIdWithInvalidDateRequestParameter() throws Exception {
        // given
        long themeId = 1L;
        String invalidDateRequestParameter = "invalid-date";

        // when & then
        mockMvc.perform(get("/times/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("date", invalidDateRequestParameter)
                        .param("themeId", Long.toString(themeId)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
