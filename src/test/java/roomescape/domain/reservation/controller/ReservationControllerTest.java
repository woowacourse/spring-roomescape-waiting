package roomescape.domain.reservation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.ReservationEditableStatus;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.reservation.mapper.ReservationMapper;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.time.dto.response.ReservationTimeResponseDto;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationMapper reservationMapper;

    private ReservationCreateCommand sampleCreateCommand() {
        return new ReservationCreateCommand(
            ReservationFixture.FUTURE.getName(),
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

    private ReservationResponseDto sampleGetResponse() {
        return new ReservationResponseDto(
            1L, ReservationFixture.FUTURE.getName(), ReservationFixture.FUTURE.getDate(),
            new ReservationTimeResponseDto(1L, TimeFixture.VALID_10_00.getStartAt(), false),
            new ReservationThemeResponseDto(1L, ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(), ThemeFixture.VALID.getImageUrl(), false),
            ReservationEditableStatus.EDITABLE, "", null, 0L
        );
    }

    @Nested
    class 이름으로_예약_조회 {

        @Test
        void 이름으로_예약_목록을_조회한다() throws Exception {
            when(reservationService.getReservationsByName(ReservationFixture.FUTURE.getName()))
                .thenReturn(List.of(sampleGetResponse()));

            mockMvc.perform(get("/api/reservations")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo(ReservationFixture.FUTURE.getName())));
        }

        @Test
        void name_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/api/reservations"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 빈_name으로_조회하면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/api/reservations")
                    .queryParam("name", ""))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_생성 {

        @Test
        void 예약을_생성한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(sampleCreateCommand());
            when(reservationService.saveReservation(any())).thenReturn(sampleCreateResponse());

            mockMvc.perform(post("/api/reservations")
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
                .andExpect(jsonPath("$.name", equalTo(ReservationFixture.FUTURE.getName())))
                .andExpect(jsonPath("$.date", equalTo(ReservationFixture.FUTURE.getDate().toString())));
        }

        @Test
        void 예약자명이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "date": "%s",
                          "timeId": 1,
                          "themeId": 1
                        }
                        """.formatted(ReservationFixture.FUTURE.getDate())))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 과거_날짜로_예약하면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s",
                          "date": "%s",
                          "timeId": 1,
                          "themeId": 1
                        }
                        """.formatted(
                        ReservationFixture.PAST.getName(),
                        ReservationFixture.PAST.getDate()
                    )))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 존재하지_않는_timeId와_themeId이면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(
                new ReservationCreateCommand("예약자", ReservationFixture.FUTURE.getDate(), 999L, 999L)
            );
            when(reservationService.saveReservation(any())).thenThrow(new GeneralParametersException(
                ReservationErrorType.FIELD_RESOURCE_NOT_FOUND,
                List.of(
                    new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."),
                    new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")
                )
            ));

            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "예약자",
                          "date": "%s",
                          "timeId": 999,
                          "themeId": 999
                        }
                        """.formatted(ReservationFixture.FUTURE.getDate())))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 이미_예약된_날짜_시간_테마이면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(sampleCreateCommand());
            when(reservationService.saveReservation(any()))
                .thenThrow(new GeneralException(ReservationErrorType.ALREADY_RESERVED));

            mockMvc.perform(post("/api/reservations")
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
    }

    @Nested
    class 예약_수정 {

        @Test
        void 예약을_수정한다() throws Exception {
            when(reservationMapper.toUpdateCommand(any())).thenReturn(
                new ReservationUpdateCommand(null, 2L, null, 0L)
            );
            when(reservationService.updateReservation(eq(1L), eq("예약자"), any())).thenReturn(
                new ReservationCreateResponseDto(1L, "예약자", ReservationFixture.FUTURE.getDate(), 2L, 1L)
            );

            mockMvc.perform(patch("/api/reservations/1")
                    .queryParam("name", "예약자")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"timeId\": 2, \"version\": 0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeId", equalTo(2)));
        }

        @Test
        void name_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"timeId\": 2, \"version\": 0}"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 존재하지_않는_예약_ID이면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toUpdateCommand(any())).thenReturn(
                new ReservationUpdateCommand(null, null, null, 0L)
            );
            when(reservationService.updateReservation(eq(999L), eq("예약자"), any()))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

            mockMvc.perform(patch("/api/reservations/999")
                    .queryParam("name", "예약자")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"version\": 0}"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_일치하지_않으면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toUpdateCommand(any())).thenReturn(
                new ReservationUpdateCommand(null, null, null, 0L)
            );
            when(reservationService.updateReservation(eq(1L), eq("다른예약자"), any()))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN));

            mockMvc.perform(patch("/api/reservations/1")
                    .queryParam("name", "다른예약자")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"version\": 0}"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void version이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .queryParam("name", "예약자")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"timeId\": 2}"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_취소 {

        @Test
        void 예약을_취소한다() throws Exception {
            when(reservationService.cancelReservation(
                1L, ReservationFixture.FUTURE.getName()))
                .thenReturn(sampleCancelResponse());

            mockMvc.perform(patch("/api/reservations/1/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
        }

        @Test
        void name_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1/cancel"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 존재하지_않는_예약_ID이면_4xx를_반환한다() throws Exception {
            when(reservationService.cancelReservation(
                999L, ReservationFixture.FUTURE.getName()))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

            mockMvc.perform(patch("/api/reservations/999/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_일치하지_않으면_4xx를_반환한다() throws Exception {
            when(reservationService.cancelReservation(1L, "다른예약자"))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN));

            mockMvc.perform(patch("/api/reservations/1/cancel")
                    .queryParam("name", "다른예약자"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_대기_생성 {

        @Test
        void 예약_대기를_생성한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(sampleCreateCommand());
            when(reservationService.saveWaitingReservation(any())).thenReturn(sampleCreateResponse());

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
            when(reservationService.saveWaitingReservation(any()))
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
    }

    @Nested
    class 예약_대기_취소 {

        @Test
        void 예약_대기를_취소한다() throws Exception {
            when(reservationService.cancelWaitingReservation(
                1L, ReservationFixture.FUTURE.getName()))
                .thenReturn(sampleCancelResponse());

            mockMvc.perform(patch("/api/reservations/1/waitings/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));
        }

        @Test
        void 존재하지_않는_예약_ID이면_4xx를_반환한다() throws Exception {
            when(reservationService.cancelWaitingReservation(
                999L, ReservationFixture.FUTURE.getName()))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

            mockMvc.perform(patch("/api/reservations/999/waitings/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_일치하지_않으면_4xx를_반환한다() throws Exception {
            when(reservationService.cancelWaitingReservation(1L, "다른예약자"))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN));

            mockMvc.perform(patch("/api/reservations/1/waitings/cancel")
                    .queryParam("name", "다른예약자"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 대기_예약이_아닌_일반_예약에_대기_취소를_시도하면_4xx를_반환한다() throws Exception {
            when(reservationService.cancelWaitingReservation(
                1L, ReservationFixture.FUTURE.getName()))
                .thenThrow(new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION));

            mockMvc.perform(patch("/api/reservations/1/waitings/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().is4xxClientError());
        }
    }
}
