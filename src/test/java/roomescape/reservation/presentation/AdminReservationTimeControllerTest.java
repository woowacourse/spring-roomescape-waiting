package roomescape.reservation.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.presentation.request.CreateTimeRequest;
import roomescape.reservation.presentation.response.CreateTimeResponse;
import roomescape.reservation.presentation.response.ReservationTimeResponse;
import roomescape.common.auth.AdminRequestValidator;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.errors.ReservationTimeErrors;

@WebMvcTest(AdminReservationTimeController.class)
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자가 전체 예약 시간 조회 시 요청과 응답을 확인한다.")
    void getAllReservationTime() throws Exception {
        // given
        ReservationTimeResponse response = new ReservationTimeResponse(
            1L,
            LocalTime.of(10, 10)
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(reservationTimeService.getAllReservationTime())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/times")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].startAt").value("10:10"));

        verify(reservationTimeService).getAllReservationTime();
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 전체 예약 시간 조회 시 401을 반환한다.")
    void getAllReservationTimeWhenUnauthorized() throws Exception {
        // given
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(get("/admin/times")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationTimeService, never()).getAllReservationTime();
    }

    @Test
    @DisplayName("관리자가 예약 시간 생성 시 요청과 응답을 확인한다.")
    void createReservationTime() throws Exception {
        // given
        CreateTimeRequest request = new CreateTimeRequest(
            LocalTime.of(10, 10)
        );
        CreateTimeResponse response = new CreateTimeResponse(
            1L,
            LocalTime.of(10, 10)
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(reservationTimeService.createReservationTime(any(CreateTimeRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/times")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.startAt").value("10:10"));

        verify(reservationTimeService).createReservationTime(request);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 예약 시간 생성 시 401을 반환한다.")
    void createReservationTimeWhenUnauthorized() throws Exception {
        // given
        CreateTimeRequest request = new CreateTimeRequest(
            LocalTime.of(10, 10)
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/admin/times")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(reservationTimeService, never()).createReservationTime(any(CreateTimeRequest.class));
    }

    @Test
    @DisplayName("예약 시간 생성 시 필수 요청값이 누락되면 예외가 발생한다.")
    void createReservationTimeWithoutStartAt() throws Exception {
        // given
        String request = """
            {
              "startAt": null
            }
            """;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(post("/admin/times")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token")
                .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INPUT_VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("시간은 필수 사항 입니다. 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("관리자가 예약 시간 삭제 시 요청과 응답을 확인한다.")
    void deleteReservationTime() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/times/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isOk());

        verify(reservationTimeService).deleteReservationTime(id);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 예약 시간 삭제 시 401을 반환한다.")
    void deleteReservationTimeWhenUnauthorized() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/admin/times/{id}", id)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationTimeService, never()).deleteReservationTime(id);
    }

    @Test
    @DisplayName("이미 예약이 존재하는 시간대는 삭제할 수 없다.")
    void deleteReservationTimeWhenTimeInUse() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        willThrow(new ConflictException(ReservationTimeErrors.RESERVATION_TIME_IN_USE))
            .given(reservationTimeService)
            .deleteReservationTime(id);

        // when & then
        mockMvc.perform(delete("/admin/times/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("RESERVATION_TIME_IN_USE"))
            .andExpect(jsonPath("$.message").value("이미 예약이 존재하는 시간대는 삭제할 수 없습니다."));
    }
}
