package roomescape.domain.reservationdate.admin;

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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationdate.admin.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateRequest;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateResponse;
import roomescape.support.auth.AdminRequestValidator;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.errors.ReservationDateErrors;

@WebMvcTest(AdminReservationDateController.class)
class AdminReservationDateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationDateService reservationDateService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자가 전체 예약 날짜 조회 시 요청과 응답을 확인한다.")
    void getAllReservationDatesForAdmin() throws Exception {
        // given
        AdminReservationDateResponse response = new AdminReservationDateResponse(
            1L,
            LocalDate.of(2026, 5, 20)
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(reservationDateService.getAllReservationDateForAdmin())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/reservation-dates")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].reservationDate").value("2026-05-20"));

        verify(reservationDateService).getAllReservationDateForAdmin();
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 전체 예약 날짜 조회 시 401을 반환한다.")
    void getAllReservationDatesForAdminWhenUnauthorized() throws Exception {
        // given
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(get("/admin/reservation-dates")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationDateService, never()).getAllReservationDateForAdmin();
    }

    @Test
    @DisplayName("관리자가 예약 날짜 생성 시 요청과 응답을 확인한다.")
    void createReservationDate() throws Exception {
        // given
        CreateReservationDateRequest request = new CreateReservationDateRequest(
            LocalDate.of(2026, 5, 20)
        );
        CreateReservationDateResponse response = new CreateReservationDateResponse(
            1L,
            LocalDate.of(2026, 5, 20)
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(reservationDateService.createReservationDate(any(CreateReservationDateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/reservation-dates")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.reservationDate").value("2026-05-20"));

        verify(reservationDateService).createReservationDate(request);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 예약 날짜 생성 시 401을 반환한다.")
    void createReservationDateWhenUnauthorized() throws Exception {
        // given
        CreateReservationDateRequest request = new CreateReservationDateRequest(
            LocalDate.of(2026, 5, 20)
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/admin/reservation-dates")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(reservationDateService, never()).createReservationDate(any(CreateReservationDateRequest.class));
    }

    @Test
    @DisplayName("예약 날짜 생성 시 필수 요청값이 누락되면 예외가 발생한다.")
    void createReservationDateWithoutReservationDate() throws Exception {
        // given
        String request = """
            {
              "reservationDate": null
            }
            """;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(post("/admin/reservation-dates")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token")
                .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INPUT_VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("예약 날짜는 필수 사항 입니다. 날짜를 선택해주세요."));
    }

    @Test
    @DisplayName("관리자가 예약 날짜 삭제 시 요청과 응답을 확인한다.")
    void deleteReservationDate() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/reservation-dates/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isNoContent());

        verify(reservationDateService).deleteReservationDate(id);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 예약 날짜 삭제 시 401을 반환한다.")
    void deleteReservationDateWhenUnauthorized() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/admin/reservation-dates/{id}", id)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationDateService, never()).deleteReservationDate(id);
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜는 삭제할 수 없다.")
    void deleteReservationDateWhenDateInUse() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        willThrow(new ConflictException(ReservationDateErrors.RESERVATION_DATE_IN_USE))
            .given(reservationDateService)
            .deleteReservationDate(id);

        // when & then
        mockMvc.perform(delete("/admin/reservation-dates/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("RESERVATION_DATE_IN_USE"))
            .andExpect(jsonPath("$.message").value("이미 예약이 존재하는 날짜는 삭제할 수 없습니다."));
    }
}
