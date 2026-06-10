package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.UpdateReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.NotOwnerException;
import roomescape.exception.NotFoundException;
import roomescape.exception.GlobalExceptionHandler;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("전체 예약을 조회하고 200 상태 코드를 반환한다.")
    void 전체_예약_조회() throws Exception {
        given(reservationService.findAllReservations()).willReturn(List.of(createMockReservation()));
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationResponses").isArray());
    }

    @Test
    @DisplayName("식별자를 통해 단건 예약을 조회하고 200 상태 코드를 반환한다.")
    void 식별자로_예약_조회() throws Exception {
        given(reservationService.getReservationById(anyLong())).willReturn(createMockReservation());
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    @DisplayName("필수 쿼리 파라미터(userName) 누락 시 400 상태 코드를 반환한다.")
    void 필수_쿼리_파라미터_누락_예외_발생() throws Exception {
        mockMvc.perform(delete("/reservations/1")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON 형식이 잘못된 경우 400 상태 코드를 반환한다.")
    void 잘못된_JSON_형식_예외_발생() throws Exception {
        String invalidJson = "{\"name\": \"브라운\", \"date\": \"잘못된날짜포맷\"}";
        performPostWithRawJson("/reservations", invalidJson)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST_FORMAT"))
                .andExpect(jsonPath("$.detail").value("요청 본문의 형식이 잘못되었습니다."));
    }

    @Test
    @DisplayName("유효한 데이터로 예약을 생성하고 201 상태 코드와 Location 헤더를 반환한다.")
    void 예약_생성() throws Exception {
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.now(), 1L, 1L);
        given(reservationService.saveReservation(any(), any(), anyLong(), anyLong(), any()))
                .willReturn(createMockReservation());
        performPost("/reservations", request).andExpect(status().isCreated()).andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("DTO 검증 실패 시 400 예외와 ProblemDetail 포맷을 반환한다.")
    void 예약_생성_DTO_검증() throws Exception {
        ReservationRequest request = new ReservationRequest("", LocalDate.now(), 1L, 1L);
        performPost("/reservations", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.detail").value("요청 값이 유효하지 않습니다."))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("존재하지 않는 자원 요청 시 404 예외와 커스텀 코드를 반환한다.")
    void 존재하지_않는_예약_조회_예외_발생() throws Exception {
        given(reservationService.getReservationById(anyLong()))
                .willThrow(new NotFoundException("해당 예약을 찾을 수 없습니다."));
        mockMvc.perform(get("/reservations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("해당 예약을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("예약 날짜와 시간을 수정하고 200 상태 코드를 반환한다.")
    void 예약_날짜_시간_수정() throws Exception {
        UpdateReservationRequest request = new UpdateReservationRequest(LocalDate.now(), 1L);
        given(reservationService.getReservationById(anyLong())).willReturn(createMockReservation());
        performReschedule("/reservations/1", "브라운", request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약을 삭제하고 204 상태 코드를 반환한다.")
    void 예약_삭제() throws Exception {
        performDelete("/reservations/1", "브라운").andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("권한이 없는 예약 제어 시 403 예외와 커스텀 코드를 반환한다.")
    void 예약_소유자_불일치_예외_발생() throws Exception {
        doThrow(new NotOwnerException()).when(reservationService).removeReservation(anyLong(), any(), any());
        performDelete("/reservations/1", "해커")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_OWNER"));
    }

    @Test
    @DisplayName("도메인 검증 실패(IllegalArgument) 시 400 상태 코드를 반환한다.")
    void 예약_생성_도메인_검증_예외_발생() throws Exception {
        given(reservationService.saveReservation(any(), any(), anyLong(), anyLong(), any()))
                .willThrow(new IllegalArgumentException("테스트용 에러 메시지"));
        performPost("/reservations", new ReservationRequest("브라운", LocalDate.now(), 1L, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DOMAIN_STATE"))
                .andExpect(jsonPath("$.detail").value("테스트용 에러 메시지"));
    }

    private ResultActions performPost(String url, Object request) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions performPostWithRawJson(String url, String jsonBody) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(jsonBody));
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
        ReservationSlot slot = new ReservationSlot(1L, LocalDate.now(), timeSlot, theme);
        return new Reservation(1L, "브라운", slot, LocalDate.now().atStartOfDay(), ReservationStatus.RESERVED);
    }
}
