package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.reservation.controller.dto.AvailableTimeResponse;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.util.ControllerTest;

@DisplayName("예약 시간 API 통합 테스트")
class ReservationTimeControllerTest extends ControllerTest {

    @DisplayName("관리자 예약 생성 시, 201을 반환한다.")
    @Test
    void create() throws Exception {
        //given
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);

        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        //when
        doReturn(reservationTimeResponse)
                .when(reservationTimeService)
                .create(any());

        //then
        mockMvc.perform(
                post("/times")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isCreated());
    }

    @DisplayName("시간 조회 시, 200을 반환한다.")
    @Test
    void findAll() throws Exception {
        //given
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);

        //when
        doReturn(List.of(reservationTimeResponse))
                .when(reservationTimeService)
                .findAll();

        //then
        mockMvc.perform(
                get("/times")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("시간 삭제 시, 204을 반환한다.")
    @Test
    void deleteTest() throws Exception {
        //given
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);

        //when
        doNothing()
                .when(reservationTimeService)
                .delete(isA(Long.class));

        //then
        mockMvc.perform(
                delete("/times/" + reservationTimeResponse.id())
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("예약이 있는 시간 삭제 시, 400을 반환한다.")
    @Test
    void delete_WithReservationTime() throws Exception {
        //given
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);

        //when
        doThrow(new BadRequestException(ErrorType.RESERVATION_NOT_DELETED))
                .when(reservationTimeService)
                .delete(isA(Long.class));

        //then
        mockMvc.perform(
                delete("/times/" + reservationTimeResponse.id())
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("가능한 시간 조회 시, 200을 반환한다.")
    @Test
    void getAvailable() throws Exception {
        //given
        AvailableTimeResponse availableTimeResponse = new AvailableTimeResponse(1L, LocalTime.NOON, true);

        //when
        doReturn(List.of(availableTimeResponse))
                .when(reservationTimeService)
                .findAvailableTimes(isA(LocalDate.class), isA(Long.class));

        //then
        mockMvc.perform(
                get("/times")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
