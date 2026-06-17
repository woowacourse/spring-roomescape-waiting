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
import org.junit.jupiter.api.DisplayName;
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
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.dto.reservation.command.CreateReservationCommand;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.command.UpdateReservationCommand;
import roomescape.fixture.Fixtures;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.OrderService;
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

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("GET /reservations/mine - 로그인 사용자의 예약 목록을 응답한다")
    void getMyReservationsRespondsWithLoginUserReservations() throws Exception {
        given(reservationService.getMyReservationStatuses(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20))
                .willReturn(ReservationWithStatusResponses.of(List.of(
                        new ReservationWithWaitingOrder(Fixtures.sampleReservation(1L), 1),
                        new ReservationWithWaitingOrder(Fixtures.sampleWaitingReservation(2L), 1)), false));

        mockMvc.perform(get("/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(reservationService).getMyReservationStatuses(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20);
    }

    @Test
    @DisplayName("GET /reservations/mine - 예약 확정과 예약 대기 목록을 구분해서 응답한다")
    void getMyReservationsSeparatesReservedAndWaiting() throws Exception {
        given(reservationService.getMyReservationStatuses(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20))
                .willReturn(ReservationWithStatusResponses.of(List.of(
                        new ReservationWithWaitingOrder(Fixtures.sampleReservation(1L), 1),
                        new ReservationWithWaitingOrder(Fixtures.sampleWaitingReservation(2L), 1)), false));

        mockMvc.perform(get("/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waitingReservations.size()").value(1))
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.waitingReservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.waitingReservations[0].waitingOrder").value(1));

        verify(reservationService).getMyReservationStatuses(LoginUserIdTestResolverConfig.FIXED_USER, 0, 20);
    }


    @Test
    @DisplayName("GET /reservations/{id} - 단건을 응답한다")
    void getReservationRespondsWithSingleReservation() throws Exception {
        given(reservationService.getReservation(1L)).willReturn(Fixtures.sampleReservation(1L));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    @DisplayName("POST /reservations - 생성된 id를 Location 헤더에 담아 201을 반환한다")
    void createReservationReturns201WithLocationHeader() throws Exception {
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
    @DisplayName("POST /reservations - 서비스가 DuplicateReservationException을 던지면 409과 메시지를 반환한다")
    void createReservationReturns409OnDuplicateReservationException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.DUPLICATE_RESERVATION, "해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."))
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
    @DisplayName("POST /reservations - 서비스가 PastDateTimeReservationException을 던지면 422와 메시지를 반환한다")
    void createReservationReturns422OnPastDateTimeReservationException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION, "예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."))
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
    @DisplayName("GET /reservations/{id} - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void getReservationReturns404OnResourceNotFoundException() throws Exception {
        given(reservationService.getReservation(9999L))
                .willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=9999"));

        mockMvc.perform(get("/reservations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("서비스에서 예상치 못한 예외가 발생하면 500과 메시지를 반환한다")
    void returns500OnUnexpectedException() throws Exception {
        given(reservationService.getReservation(1L))
                .willThrow(new RuntimeException("예기치 못한 오류"));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 200을 반환하고 로그인 사용자로 서비스에 위임한다")
    void deleteReservationReturns200AndDelegatesWithLoginUser() throws Exception {
        mockMvc.perform(post("/reservations/3/cancel"))
                .andExpect(status().isOk());

        verify(reservationService).deleteOwnReservation(Fixtures.deleteCommand(3L, 1L));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void deleteReservationReturns404OnResourceNotFoundException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=9999"))
                .given(reservationService).deleteOwnReservation(Fixtures.deleteCommand(9999L, 1L));

        mockMvc.perform(post("/reservations/9999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 소유자 불일치면 403과 메시지를 반환한다")
    void deleteReservationReturns403OnOwnerMismatch() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_OWNER_MISMATCH, "본인의 예약만 취소 혹은 변경 가능합니다."))
                .given(reservationService).deleteOwnReservation(Fixtures.deleteCommand(1L, 1L));

        mockMvc.perform(post("/reservations/1/cancel"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESERVATION_OWNER_MISMATCH"));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 200을 반환하고 로그인 사용자로 서비스에 위임한다")
    void updateReservationReturns200AndDelegatesWithLoginUser() throws Exception {
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
    @DisplayName("PUT /reservations/{id} - 서비스가 ReservationOwnerMismatchException을 던지면 403과 메시지를 반환한다")
    void updateReservationReturns403OnReservationOwnerMismatchException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_OWNER_MISMATCH, "본인의 예약만 취소 혹은 변경 가능합니다."))
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
    @DisplayName("PUT /reservations/{id} - 서비스가 PastDateTimeReservationException을 던지면 422과 메시지를 반환한다")
    void updateReservationReturns422OnPastDateTimeReservationException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION, "예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."))
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
    @DisplayName("PUT /reservations/{id} - 서비스가 DuplicateReservationException을 던지면 409과 메시지를 반환한다")
    void updateReservationReturns409OnDuplicateReservationException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.DUPLICATE_RESERVATION, "해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."))
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
    @DisplayName("PUT /reservations/{id} - 서비스가 ReservationNotReservedException을 던지면 409과 메시지를 반환한다")
    void updateReservationReturns409OnReservationNotReservedException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_NOT_RESERVED, "해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: WAITING"))
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
    @DisplayName("POST /reservations - 본문의 date가 형식 오류면 400과 메시지를 반환한다")
    void createReservationReturns400OnInvalidDateFormat() throws Exception {
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
    @DisplayName("GET /reservations/{id} - id가 숫자가 아니면 400과 메시지를 반환한다")
    void getReservationReturns400WhenIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/reservations/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /reservations - 본문의 date가 존재하지 않는 날짜면 400과 메시지를 반환한다")
    void createReservationReturns400WhenDateDoesNotExist() throws Exception {
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
    @DisplayName("POST /reservations - 본문의 timeId가 숫자가 아니면 400과 메시지를 반환한다")
    void createReservationReturns400WhenTimeIdIsNotNumber() throws Exception {
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
    @DisplayName("POST /reservations - 본문의 themeId가 누락되면 400과 메시지를 반환한다")
    void createReservationReturns400WhenThemeIdIsMissing() throws Exception {
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
    @DisplayName("POST /reservations - 본문의 storeId가 누락되면 400과 메시지를 반환한다")
    void createReservationReturns400WhenStoreIdIsMissing() throws Exception {
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
    @DisplayName("POST /reservations - 본문 JSON 문법 오류면 400과 메시지를 반환한다")
    void createReservationReturns400OnMalformedJson() throws Exception {
        String brokenBody = "{\"themeId\":1";

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /reservations/waiting - 생성된 대기 예약을 body에 담고 Location 헤더와 함께 201을 반환한다")
    void createWaitingReservationReturns201WithBodyAndLocationHeader() throws Exception {
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
    @DisplayName("POST /reservations/waiting - 서비스가 ReservationNotFoundForWaitingException을 던지면 409과 메시지를 반환한다")
    void createWaitingReservationReturns409OnReservationNotFoundForWaitingException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_NOT_FOUND_FOR_WAITING, "확정 예약이 없으므로 대기 예약 생성이 불가능합니다."))
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
    @DisplayName("POST /reservations/waiting - 서비스가 PastDateTimeReservationException을 던지면 422과 메시지를 반환한다")
    void createWaitingReservationReturns422OnPastDateTimeReservationException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION, "예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."))
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
    @DisplayName("POST /reservations/waiting - 서비스가 DuplicateWaitingReservationException을 던지면 409과 메시지를 반환한다")
    void createWaitingReservationReturns409OnDuplicateWaitingReservationException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.DUPLICATE_WAITING_RESERVATION, "이미 해당 슬롯에 예약 대기 중입니다."))
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
