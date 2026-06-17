package roomescape.feature.reservation.controller;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.service.ReservationService;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.theme.dto.response.ReservationThemeResponseDto;
import roomescape.feature.time.dto.response.ReservationTimeResponseDto;
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

    private ReservationResponseDto sampleGetResponse() {
        return new ReservationResponseDto(
            1L, ReservationFixture.FUTURE.getName(), ReservationFixture.FUTURE.getDate(),
            new ReservationTimeResponseDto(1L, TimeFixture.VALID_10_00.getStartAt(), false),
            new ReservationThemeResponseDto(1L, ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(), ThemeFixture.VALID.getImageUrl(), false),
            ReservationEditableStatus.EDITABLE, "", null, OrderStatus.CONFIRMED,
            "test_order_id", "test_payment_key", 1_000L
        );
    }

    @Nested
    class 이름으로_예약_조회 {

        @Test
        void 이름으로_예약_목록을_조회한다() throws Exception {
            when(reservationService.getReservationsByName(new ReserverName(ReservationFixture.FUTURE.getName())))
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
        void 존재하지_않는_timeId와_themeId이면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toCreateCommand(any())).thenReturn(
                new ReservationCreateCommand(new ReserverName("예약자"), ReservationFixture.FUTURE.getDate(), 999L, 999L)
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

        @Test
        void 예약자명이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(OMITTED, VALID_DATE, "1", "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약_날짜가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, OMITTED, "1", "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void timeId가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, OMITTED, "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void themeId가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, "1", OMITTED)))
                .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void timeId가_양수가_아니면_4xx를_반환한다(long timeId) throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, String.valueOf(timeId), "1")))
                .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void themeId가_양수가_아니면_4xx를_반환한다(long themeId) throws Exception {
            mockMvc.perform(post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, "1", String.valueOf(themeId))))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_수정 {

        @Test
        void 예약을_수정한다() throws Exception {
            when(reservationMapper.toUpdateCommand(any())).thenReturn(
                new ReservationUpdateCommand(new ReserverName("예약자"), ReservationFixture.FUTURE.getDate(), 2L, 1L)
            );
            when(reservationService.updateReservation(eq(1L), any())).thenReturn(
                new ReservationCreateResponseDto(1L, "예약자", ReservationFixture.FUTURE.getDate(), 2L, 1L)
            );

            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "예약자",
                          "date": "%s",
                          "timeId": 2,
                          "themeId": 1
                        }
                        """.formatted(ReservationFixture.FUTURE.getDate())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeId", equalTo(2)));
        }

        @Test
        void 존재하지_않는_예약_ID이면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toUpdateCommand(any())).thenReturn(
                new ReservationUpdateCommand(new ReserverName("예약자"), ReservationFixture.FUTURE.getDate(), 1L, 1L)
            );
            when(reservationService.updateReservation(eq(999L), any()))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

            mockMvc.perform(patch("/api/reservations/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "예약자",
                          "date": "%s",
                          "timeId": 1,
                          "themeId": 1
                        }
                        """.formatted(ReservationFixture.FUTURE.getDate())))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_일치하지_않으면_4xx를_반환한다() throws Exception {
            when(reservationMapper.toUpdateCommand(any())).thenReturn(
                new ReservationUpdateCommand(new ReserverName("다른예약자"), ReservationFixture.FUTURE.getDate(), 1L, 1L)
            );
            when(reservationService.updateReservation(eq(1L), any()))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN));

            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "다른예약자",
                          "date": "%s",
                          "timeId": 1,
                          "themeId": 1
                        }
                        """.formatted(ReservationFixture.FUTURE.getDate())))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(OMITTED, VALID_DATE, "1", "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약_날짜가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, OMITTED, "1", "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void timeId가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, OMITTED, "1")))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void themeId가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, "1", OMITTED)))
                .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void timeId가_양수가_아니면_4xx를_반환한다(long timeId) throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, String.valueOf(timeId), "1")))
                .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void themeId가_양수가_아니면_4xx를_반환한다(long themeId) throws Exception {
            mockMvc.perform(patch("/api/reservations/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationRequestBody(VALID_NAME, VALID_DATE, "1", String.valueOf(themeId))))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_취소 {

        @Test
        void 예약을_취소한다() throws Exception {
            when(reservationService.cancelReservation(eq(1L), eq(new ReserverName(ReservationFixture.FUTURE.getName()))))
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
            when(reservationService.cancelReservation(eq(999L), eq(new ReserverName(ReservationFixture.FUTURE.getName()))))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));

            mockMvc.perform(patch("/api/reservations/999/cancel")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 예약자명이_일치하지_않으면_4xx를_반환한다() throws Exception {
            when(reservationService.cancelReservation(eq(1L), eq(new ReserverName("다른예약자"))))
                .thenThrow(new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN));

            mockMvc.perform(patch("/api/reservations/1/cancel")
                    .queryParam("name", "다른예약자"))
                .andExpect(status().is4xxClientError());
        }
    }
}
