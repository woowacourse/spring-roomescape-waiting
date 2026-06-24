package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.controller.dto.PaymentReservationRequest;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.ProblemDetailsAdvice;
import roomescape.exception.ReservationNotFoundException;
import roomescape.service.SessionService;

@WebMvcTest(ReservationController.class)
@Import(ProblemDetailsAdvice.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @Test
    @DisplayName("전체 예약을 조회하고 200 상태 코드를 반환한다.")
    void getReservations() throws Exception {
        given(sessionService.allReservations()).willReturn(List.of(createMockReservation()));
        mockMvc.perform(get("/reservations")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("식별자를 통해 단건 예약을 조회하고 200 상태 코드를 반환한다.")
    void getReservationById() throws Exception {
        given(sessionService.findReservationById(anyLong())).willReturn(createMockReservation());
        mockMvc.perform(get("/reservations/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    @DisplayName("필수 쿼리 파라미터(userName) 누락 시 400 상태 코드를 반환한다.")
    void missingRequestParam() throws Exception {
        mockMvc.perform(delete("/reservations/1")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON 형식이 잘못된 경우 400 상태 코드를 반환한다.")
    void malformedJsonRequest() throws Exception {
        String invalidJson = "{\"name\": \"브라운\", \"date\": \"잘못된날짜포맷\"}";
        performPostWithRawJson("/reservations", invalidJson)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST_FORMAT"))
                .andExpect(jsonPath("$.detail").value("요청 본문의 형식이 잘못되었습니다."));
    }

    @Test
    @DisplayName("유효한 데이터로 예약을 생성하고 201 상태 코드와 Location 헤더를 반환한다.")
    void createReservation() throws Exception {
        given(sessionService.makeReservation(any())).willReturn(createMockReservation());
        performPost("/reservations", new PaymentReservationRequest("브라운", LocalDate.now(), 1L, 1L, 0L, "pk_test", "order_test"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("DTO 검증 실패 시 400 예외와 ProblemDetail 포맷을 반환한다.")
    void createReservationWithInvalidData() throws Exception {
        performPost("/reservations", new PaymentReservationRequest("", LocalDate.now(), 1L, 1L, 0L, "pk_test", "order_test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.detail").value("요청 값이 유효하지 않습니다."))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("예약의 전체 정보를 수정하고 200 상태 코드를 반환한다.")
    void rescheduleAll() throws Exception {
        given(sessionService.rescheduleReservation(anyLong(), anyString(), any(ReservationRequest.class)))
                .willReturn(createMockReservation());
        performPut("/reservations/1", "브라운", new ReservationRequest("네오", LocalDate.now(), 1L, 1L, 0L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 자원 요청 시 404 예외와 커스텀 코드를 반환한다.")
    void findNonExistentReservation() throws Exception {
        given(sessionService.rescheduleReservation(anyLong(), anyString(), any(ReservationRequest.class)))
                .willThrow(new ReservationNotFoundException(999L));
        performPut("/reservations/999", "브라운", new ReservationRequest("네오", LocalDate.now(), 1L, 1L, 0L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("예약의 부분 정보를 수정하고 200 상태 코드를 반환한다.")
    void reschedulePart() throws Exception {
        given(sessionService.patchReservation(anyLong(), anyString(), any(ReservationPatchRequest.class)))
                .willReturn(createMockReservation());
        performReschedule("/reservations/1", "브라운", new ReservationPatchRequest("네오", null, null, null))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약을 삭제하고 204 상태 코드를 반환한다.")
    void deleteReservation() throws Exception {
        doNothing().when(sessionService).cancelReservation(anyLong(), anyString());
        performDelete("/reservations/1", "브라운").andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("권한이 없는 예약 제어 시 403 예외와 커스텀 코드를 반환한다.")
    void deleteWithoutOwnership() throws Exception {
        doThrow(new InvalidOwnershipException()).when(sessionService).cancelReservation(anyLong(), anyString());
        performDelete("/reservations/1", "해커")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVALID_OWNERSHIP"));
    }

    @Test
    @DisplayName("도메인 검증 실패(IllegalArgument) 시 400 상태 코드를 반환한다.")
    void createReservationIllegalArgument() throws Exception {
        given(sessionService.makeReservation(any()))
                .willThrow(new IllegalArgumentException("테스트용 에러 메시지"));
        performPost("/reservations", new PaymentReservationRequest("브라운", LocalDate.now(), 1L, 1L, 0L, "pk_test", "order_test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DOMAIN_STATE"))
                .andExpect(jsonPath("$.detail").value("테스트용 에러 메시지"));
    }

    @Test
    @DisplayName("데이터 중복 발생(DuplicateKey) 시 409 상태 코드를 반환한다.")
    void createReservationDuplicateKey() throws Exception {
        given(sessionService.makeReservation(any()))
                .willThrow(new DuplicateKeyException("중복 데이터 발생"));
        performPost("/reservations", new PaymentReservationRequest("브라운", LocalDate.now(), 1L, 1L, 0L, "pk_test", "order_test"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_KEY_VIOLATION"))
                .andExpect(jsonPath("$.detail").value("이미 존재하는 데이터입니다."));
    }

    @Test
    @DisplayName("데이터 무결성 위반(DataIntegrity) 시 400 상태 코드를 반환한다.")
    void createReservationDataIntegrity() throws Exception {
        given(sessionService.makeReservation(any()))
                .willThrow(new DataIntegrityViolationException("외래키 위반"));
        performPost("/reservations", new PaymentReservationRequest("브라운", LocalDate.now(), 1L, 1L, 0L, "pk_test", "order_test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"))
                .andExpect(jsonPath("$.detail").value("데이터 무결성 제약조건이 위반되었습니다."));
    }

    private ResultActions performPost(String url, Object request) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performPostWithRawJson(String url, String jsonBody) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(jsonBody));
    }

    private ResultActions performPut(String url, String userName, Object request) throws Exception {
        return mockMvc.perform(put(url).param("userName", userName).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performReschedule(String url, String userName, Object request) throws Exception {
        return mockMvc.perform(patch(url).param("userName", userName).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performDelete(String url, String userName) throws Exception {
        return mockMvc.perform(delete(url).param("userName", userName));
    }

    private Reservation createMockReservation() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Session session = new Session(1L, LocalDate.now(), timeSlot, theme);
        return new Reservation(1L, "브라운", session, 0L, null);
    }
}
