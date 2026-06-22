package roomescape.feature.reservation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.service.WaitingService;
import roomescape.fixture.ReservationFixture;
import roomescape.global.error.exception.GeneralException;
import roomescape.support.WebMvcControllerTest;

@WebMvcControllerTest(controllers = WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;

    @MockitoBean
    private ReservationMapper reservationMapper;

    private static final String VALID_NAME = "\"%s\"".formatted(ReservationFixture.FUTURE.getName());
    private static final String VALID_DATE = "\"%s\"".formatted(ReservationFixture.FUTURE.getDate());
    private static final String OMITTED = "null";

    private String reservationRequestBody(String name, String date, String timeId, String themeId) {
        return """
            {
              "name": %s,
              "date": %s,
              "timeId": %s,
              "themeId": %s
            }
            """.formatted(name, date, timeId, themeId);
    }

    private ReservationCreateCommand sampleCreateCommand() {
        return new ReservationCreateCommand(
            new ReserverName(ReservationFixture.FUTURE.getName()),
            ReservationFixture.FUTURE.getDate(), 1L, 1L
        );
    }

    private ReservationCreateResponseDto sampleCreateResponse() {
        return new ReservationCreateResponseDto(
            1L, ReservationFixture.FUTURE.getName(), ReservationFixture.FUTURE.getDate(), 1L, 1L
        );
    }

    private ReservationCancelResponseDto sampleCancelResponse() {
        return new ReservationCancelResponseDto(
            1L, ReservationFixture.FUTURE.getName(), ReservationFixture.FUTURE.getDate(), 1L, 1L
        );
    }

    @Nested
    class 예약_대기_생성 {

        @Test
        void 예약_대기를_생성한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(sampleCreateCommand());
            when(waitingService.saveWaitingReservation(any())).thenReturn(sampleCreateResponse());

            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s",
                          "date": "%s",
                          "timeId": 1,
                          "themeId": 1
                        }
                        """.formatted(
                            ReservationFixture.FUTURE.getName(),
                            ReservationFixture.FUTURE.getDate()
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo(ReservationFixture.FUTURE.getName())));
        }

        @Test
        void 이미_대기_중인_예약이면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(sampleCreateCommand());
            when(waitingService.saveWaitingReservation(any()))
                .thenThrow(new GeneralException(ReservationErrorType.ALREADY_WAITING));

            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s",
                          "date": "%s",
                          "timeId": 1,
                          "themeId": 1
                        }
                        """.formatted(
                            ReservationFixture.FUTURE.getName(),
                            ReservationFixture.FUTURE.getDate()
                        )))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(OMITTED, VALID_DATE, "1", "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약_날짜가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, OMITTED, "1", "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void timeId가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, OMITTED, "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void themeId가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, "1", OMITTED)))
                .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void timeId가_양수가_아니면_4xx를_반환한다(long timeId) throws Exception {
            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, String.valueOf(timeId), "1")))
                .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void themeId가_양수가_아니면_4xx를_반환한다(long themeId) throws Exception {
            mockMvc.perform(post("/api/reservations/waitings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, "1", String.valueOf(themeId))))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_대기_취소 {

        @Test
        void 예약_대기를_취소한다() throws Exception {
            when(waitingService.cancelWaitingReservation(eq(1L), eq(new ReserverName(ReservationFixture.FUTURE.getName()))))
                .thenReturn(sampleCancelResponse());

            mockMvc.perform(patch("/api/reservations/waitings/1/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
        }

        @Test
        void 존재하지_않는_예약_ID이면_4xx를_반환한다() throws Exception {
            when(waitingService.cancelWaitingReservation(eq(999L), eq(new ReserverName(ReservationFixture.FUTURE.getName()))))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

            mockMvc.perform(patch("/api/reservations/waitings/999/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_일치하지_않으면_4xx를_반환한다() throws Exception {
            when(waitingService.cancelWaitingReservation(eq(1L), eq(new ReserverName("다른예약자"))))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN));

            mockMvc.perform(patch("/api/reservations/waitings/1/cancel")
                    .queryParam("name", "다른예약자"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void name_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/waitings/1/cancel"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 대기_예약이_아닌_일반_예약에_대기_취소를_시도하면_4xx를_반환한다() throws Exception {
            when(waitingService.cancelWaitingReservation(eq(1L), eq(new ReserverName(ReservationFixture.FUTURE.getName()))))
                .thenThrow(new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION));

            mockMvc.perform(patch("/api/reservations/waitings/1/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().is4xxClientError());
        }
    }
}
