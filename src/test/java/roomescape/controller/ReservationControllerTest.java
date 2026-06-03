package roomescape.controller;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationStatus;
import roomescape.dto.reservation.command.CreateReservationCommand;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.command.UpdateReservationCommand;
import roomescape.fixture.Fixtures;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ReservationService;

@WebMvcTest(controllers = ReservationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
@Import(LoginUserIdTestResolverConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void GET_reservations_mine_로그인_사용자의_예약_목록을_응답한다() throws Exception {
        given(reservationService.getMyReservations(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20))
                .willReturn(ReservationWithStatusResponses.of(List.of(Fixtures.sampleReservation(1L)),
                        Map.of(Fixtures.sampleWaitingReservation(2L), 1), false));

        mockMvc.perform(get("/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(reservationService).getMyReservations(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20);
    }

    @Test
    void GET_reservations_mine_예약_확정과_예약_대기_목록을_구분해서_응답한다() throws Exception {
        given(reservationService.getMyReservations(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20))
                .willReturn(ReservationWithStatusResponses.of(List.of(Fixtures.sampleReservation(1L)),
                        Map.of(Fixtures.sampleWaitingReservation(2L), 1), false));

        mockMvc.perform(get("/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waitingReservations.size()").value(1))
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.waitingReservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.waitingReservations[0].waitingOrder").value(1));

        verify(reservationService).getMyReservations(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20);
    }


    @Test
    void GET_reservations_id_단건을_응답한다() throws Exception {
        given(reservationService.getReservation(1L)).willReturn(Fixtures.sampleReservation(1L));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    void POST_reservations_생성된_id를_Location_헤더에_담아_201을_반환한다() throws Exception {
        given(reservationService.create(any(CreateReservationCommand.class), eq(ReservationStatus.RESERVED)))
                .willReturn(Fixtures.sampleReservation(7L));

        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/7"));
    }

    @Test
    void POST_reservations_서비스가_DuplicateReservationException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.DUPLICATE_RESERVATION))
                .given(reservationService).create(any(CreateReservationCommand.class), eq(ReservationStatus.RESERVED));

        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"));
    }

    @Test
    void POST_reservations_서비스가_PastDateTimeReservationException을_던지면_422_와_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION))
                .given(reservationService).create(any(CreateReservationCommand.class), eq(ReservationStatus.RESERVED));

        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PAST_DATE_TIME_RESERVATION"));
    }

    @Test
    void GET_reservations_id_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        given(reservationService.getReservation(9999L))
                .willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", 9999L));

        mockMvc.perform(get("/reservations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 서비스에서_예상치_못한_예외가_발생하면_500과_메시지를_반환한다() throws Exception {
        given(reservationService.getReservation(1L))
                .willThrow(new RuntimeException("예기치 못한 오류"));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void POST_reservations_id_cancel_200을_반환하고_로그인_사용자로_서비스에_위임한다() throws Exception {
        mockMvc.perform(post("/reservations/3/cancel"))
                .andExpect(status().isOk());

        verify(reservationService).cancelOwnReservation(Fixtures.cancelCommand(3L, 1L));
    }

    @Test
    void POST_reservations_id_cancel_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", 9999L))
                .given(reservationService).cancelOwnReservation(Fixtures.cancelCommand(9999L, 1L));

        mockMvc.perform(post("/reservations/9999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void POST_reservations_id_cancel_소유자_불일치면_403과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_OWNER_MISMATCH))
                .given(reservationService).cancelOwnReservation(Fixtures.cancelCommand(1L, 1L));

        mockMvc.perform(post("/reservations/1/cancel"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESERVATION_OWNER_MISMATCH"));
    }

    @Test
    void PUT_reservations_id_200을_반환하고_로그인_사용자로_서비스에_위임한다() throws Exception {
        given(reservationService.updateOwnReservation(any(UpdateReservationCommand.class)))
                .willReturn(Fixtures.sampleReservation(1L));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));
    }

    @Test
    void PUT_reservations_id_서비스가_ReservationOwnerMismatchException을_던지면_403과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_OWNER_MISMATCH))
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESERVATION_OWNER_MISMATCH"));
    }

    @Test
    void PUT_reservations_id_서비스가_PastDateTimeReservationException을_던지면_422과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION))
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-05-01",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PAST_DATE_TIME_RESERVATION"));
    }

    @Test
    void PUT_reservations_id_서비스가_DuplicateReservationException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.DUPLICATE_RESERVATION))
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"));
    }

    @Test
    void PUT_reservations_id_서비스가_ReservationNotReservedException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_NOT_RESERVED, "WAITING"))
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_RESERVED"));
    }

    @Test
    void POST_reservations_본문의_date가_형식_오류면_400과_메시지를_반환한다() throws Exception {
        String body = """
                {"date":"abc","themeId":1,"timeId":1}
                """;

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void GET_reservations_id가_숫자가_아니면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/reservations/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void POST_reservations_본문의_date가_존재하지_않는_날짜면_400과_메시지를_반환한다() throws Exception {
        String body = """
                {"date":"2026-13-40","themeId":1,"timeId":1}
                """;

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void POST_reservations_본문의_timeId가_숫자가_아니면_400과_메시지를_반환한다() throws Exception {
        String body = """
                {"date":"2026-05-08","themeId":1,"timeId":"abc"}
                """;

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void POST_reservations_본문의_themeId가_누락되면_400과_메시지를_반환한다() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-05-08");
        body.put("timeId", 1);
        body.put("storeId", 1);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void POST_reservations_본문의_storeId가_누락되면_400과_메시지를_반환한다() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-05-08");
        body.put("themeId", 1);
        body.put("timeId", 1);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void POST_reservations_본문_JSON_문법_오류면_400과_메시지를_반환한다() throws Exception {
        String brokenBody = "{\"themeId\":1";

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void POST_reservations_waiting_생성된_대기_예약을_body에_담고_Location_헤더와_함께_201을_반환한다() throws Exception {
        given(reservationService.create(any(CreateReservationCommand.class), eq(ReservationStatus.WAITING)))
                .willReturn(Fixtures.sampleWaitingReservation(7L));

        Map<String, Object> body = Map.of(
                "date", "2026-05-01",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void POST_reservations_waiting_서비스가_ReservationNotFoundForWaitingException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_NOT_FOUND_FOR_WAITING))
                .given(reservationService).create(any(CreateReservationCommand.class), eq(ReservationStatus.WAITING));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND_FOR_WAITING"));
    }

    @Test
    void POST_reservations_waiting_서비스가_PastDateTimeReservationException을_던지면_422과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION))
                .given(reservationService).create(any(CreateReservationCommand.class), eq(ReservationStatus.WAITING));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PAST_DATE_TIME_RESERVATION"));
    }

    @Test
    void POST_reservations_waiting_서비스가_DuplicateWaitingReservationException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.DUPLICATE_WAITING_RESERVATION))
                .given(reservationService).create(any(CreateReservationCommand.class), eq(ReservationStatus.WAITING));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_WAITING_RESERVATION"));
    }
}
