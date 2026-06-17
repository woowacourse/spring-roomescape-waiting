package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.response.ReservationPaymentResponse;
import roomescape.dto.response.ReservationWithStatusResponses;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingReservationException;
import roomescape.exception.PastDateTimeReservationException;
import roomescape.exception.ReservationConcurrentConflictException;
import roomescape.exception.ReservationNotFoundForWaitingException;
import roomescape.exception.ReservationNotReservedException;
import roomescape.exception.ReservationNotWaitingException;
import roomescape.exception.ReservationOwnerMismatchException;
import roomescape.fixture.Fixtures;
import roomescape.infrastructure.AdminAuthorizationInterceptor;
import roomescape.infrastructure.LoginCheckInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ReservationService;

@WebMvcTest(controllers = ReservationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, LoginCheckInterceptor.class,
                        AdminAuthorizationInterceptor.class, LoginUserArgumentResolver.class}))
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
        given(reservationService.getMyReservations(1L))
                .willReturn(ReservationWithStatusResponses.of(List.of(Fixtures.sampleReservation(1L)),
                        Map.of(Fixtures.sampleWaitingReservation(2L), 1), false));

        mockMvc.perform(get("/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(reservationService).getMyReservations(1L);
    }

    @Disabled("TODO: 예약 상태별 내 예약 조회 응답 구현 후 작성")
    @Test
    void GET_reservations_mine_예약_확정과_예약_대기_목록을_구분해서_응답한다() throws Exception {
        given(reservationService.getMyReservations(1L))
                .willReturn(ReservationWithStatusResponses.of(List.of(Fixtures.sampleReservation(1L)),
                        Map.of(Fixtures.sampleWaitingReservation(2L), 1), false));

        mockMvc.perform(get("/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waitingReservations.size()").value(1))
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.waitingReservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.waitingReservations[0].waitingOrder").value(1));

        verify(reservationService).getMyReservations(1L);
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
        given(reservationService.createReservation(any(CreateReservationCommand.class)))
                .willReturn(new ReservationPaymentResponse(7L, "order_123456", 10_000L));

        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1,
                "amount", 10_000);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/7"))
                .andExpect(jsonPath("$.reservationId").value(7))
                .andExpect(jsonPath("$.orderId").value("order_123456"))
                .andExpect(jsonPath("$.amount").value(10_000));
    }

    @Test
    void POST_reservations_서비스가_DuplicateReservationException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new DuplicateReservationException())
                .given(reservationService).createReservation(any(CreateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1,
                "amount", 10_000);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."));
    }

    @Test
    void POST_reservations_서비스가_PastDateTimeReservationException을_던지면_422_와_메시지를_반환한다() throws Exception {
        willThrow(new PastDateTimeReservationException())
                .given(reservationService).createReservation(any(CreateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1,
                "amount", 10_000);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."));
    }

    @Test
    void GET_reservations_id_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        given(reservationService.getReservation(9999L))
                .willThrow(new roomescape.exception.ResourceNotFoundException("예약", 9999L));

        mockMvc.perform(get("/reservations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void 서비스에서_예상치_못한_예외가_발생하면_500과_메시지를_반환한다() throws Exception {
        given(reservationService.getReservation(1L))
                .willThrow(new RuntimeException("예기치 못한 오류"));

        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("알 수 없는 서버 에러가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    @Test
    void DELETE_reservations_id_204를_반환하고_로그인_사용자로_서비스에_위임한다() throws Exception {
        mockMvc.perform(delete("/reservations/3"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelOwnReservation(Fixtures.cancelCommand(3L, 1L));
    }

    @Test
    void DELETE_reservations_waiting_id_204를_반환하고_로그인_사용자로_서비스에_위임한다() throws Exception {
        mockMvc.perform(delete("/reservations/waiting/3"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelOwnWaitingReservation(Fixtures.cancelCommand(3L, 1L));
    }

    @Test
    void DELETE_reservations_id_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        willThrow(new roomescape.exception.ResourceNotFoundException("예약", 9999L))
                .given(reservationService).cancelOwnReservation(Fixtures.cancelCommand(9999L, 1L));

        mockMvc.perform(delete("/reservations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void DELETE_reservations_id_소유자_불일치면_403과_메시지를_반환한다() throws Exception {
        willThrow(new ReservationOwnerMismatchException())
                .given(reservationService).cancelOwnReservation(Fixtures.cancelCommand(1L, 1L));

        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("본인의 예약만 취소 혹은 변경 가능합니다."));
    }

    @Test
    void DELETE_reservations_id_예약_정보가_동시에_변경되면_409과_메시지를_반환한다() throws Exception {
        willThrow(new ReservationConcurrentConflictException())
                .given(reservationService).cancelOwnReservation(Fixtures.cancelCommand(1L, 1L));

        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("예약 정보가 변경되어 요청을 처리할 수 없습니다. 다시 시도해주세요."));
    }

    @Test
    void DELETE_reservations_waiting_id_예약_확정이면_409과_메시지를_반환한다() throws Exception {
        willThrow(new ReservationNotWaitingException("RESERVED"))
                .given(reservationService).cancelOwnWaitingReservation(Fixtures.cancelCommand(1L, 1L));

        mockMvc.perform(delete("/reservations/waiting/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 예약은 예약 대기 상태가 아닙니다. 현재 예약 상태 값: RESERVED"));
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
        willThrow(new ReservationOwnerMismatchException())
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("본인의 예약만 취소 혹은 변경 가능합니다."));
    }

    @Test
    void PUT_reservations_id_서비스가_PastDateTimeReservationException을_던지면_422과_메시지를_반환한다() throws Exception {
        willThrow(new PastDateTimeReservationException())
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-05-01",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."));
    }

    @Test
    void PUT_reservations_id_서비스가_DuplicateReservationException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new DuplicateReservationException())
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."));
    }

    @Test
    void PUT_reservations_id_서비스가_ReservationNotReservedException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new ReservationNotReservedException("WAITING"))
                .given(reservationService).updateOwnReservation(any(UpdateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1);

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: WAITING"));
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
                .andExpect(jsonPath("$.message")
                        .value("'date' 값 'abc'은(는) yyyy-MM-dd 형식이어야 합니다."));
    }

    @Test
    void GET_reservations_id가_숫자가_아니면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/reservations/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("'id' 값 'abc'은(는) 숫자 형식이어야 합니다."));
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
                .andExpect(jsonPath("$.message")
                        .value("'date' 값 '2026-13-40'은(는) yyyy-MM-dd 형식이어야 합니다."));
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
                .andExpect(jsonPath("$.message")
                        .value("'timeId' 값 'abc'은(는) 숫자 형식이어야 합니다."));
    }

    @Test
    void POST_reservations_본문의_themeId가_누락되면_400과_메시지를_반환한다() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-05-08");
        body.put("timeId", 1);
        body.put("storeId", 1);
        body.put("amount", 10_000);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("themeId은(는) 필수 입력값입니다."));
    }

    @Test
    void POST_reservations_본문의_storeId가_누락되면_400과_메시지를_반환한다() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("date", "2026-05-08");
        body.put("themeId", 1);
        body.put("timeId", 1);
        body.put("amount", 10_000);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("storeId은(는) 필수 입력값입니다."));
    }

    @Test
    void POST_reservations_본문_JSON_문법_오류면_400과_메시지를_반환한다() throws Exception {
        String brokenBody = "{\"themeId\":1";

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 본문 형식이 올바르지 않습니다."));
    }

    @Test
    void POST_reservations_waiting_생성된_id를_Location_헤더에_담아_201을_반환한다() throws Exception {
        given(reservationService.createWaitingReservation(any(CreateReservationCommand.class)))
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
                .andExpect(header().string("Location", "/reservations/7"));
    }

    @Test
    void POST_reservations_waiting_서비스가_ReservationNotFoundForWaitingException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new ReservationNotFoundForWaitingException())
                .given(reservationService).createWaitingReservation(any(CreateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("확정 예약이 없으므로 대기 예약 생성이 불가능합니다."));
    }

    @Test
    void POST_reservations_waiting_서비스가_PastDateTimeReservationException을_던지면_422과_메시지를_반환한다() throws Exception {
        willThrow(new PastDateTimeReservationException())
                .given(reservationService).createWaitingReservation(any(CreateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."));
    }

    @Test
    void POST_reservations_waiting_서비스가_DuplicateWaitingReservationException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new DuplicateWaitingReservationException())
                .given(reservationService).createWaitingReservation(any(CreateReservationCommand.class));

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", 1,
                "timeId", 1,
                "storeId", 1);

        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 해당 슬롯에 예약 대기 중입니다."));
    }
}
