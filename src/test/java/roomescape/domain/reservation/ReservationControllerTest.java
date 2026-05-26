package roomescape.domain.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservation.dto.CreateReservationRequest;
import roomescape.domain.reservation.dto.CreateReservationResponse;
import roomescape.domain.reservation.dto.CreateReservationResponse.ThemePayload;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.UserReservationResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationErrors;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 생성 요청과 응답을 확인한다.")
    void createReservation() throws Exception {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            1L,
            2L,
            3L
        );
        CreateReservationResponse response = new CreateReservationResponse(
            10L,
            LocalDate.of(2026, 5, 20),
            LocalTime.of(10, 0),
            ThemePayload.from(Theme.of(1L, "공포", "무섭다", "theme-url"))
        );
        given(reservationService.createReservation(any(CreateReservationRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.date").value("2026-05-20"))
            .andExpect(jsonPath("$.time").value("10:00"))
            .andExpect(jsonPath("$.theme.name").value("공포"))
            .andExpect(jsonPath("$.theme.content").value("무섭다"))
            .andExpect(jsonPath("$.theme.url").value("theme-url"));
    }

    @Test
    @DisplayName("필수 파라미터가 누락되었을 때 예외가 발생한다.")
    void createWrongParameterReservation() throws Exception {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
            "보예",
            1L,
            null,
            3L
        );

        // when & then
        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INPUT_VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("시간은 필수 선택 사항 입니다. 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("예약자 이름 조회의 요청과 응답을 확인한다.")
    void getUserReservations() throws Exception {
        // given
        String name = "보예";
        UserReservationResponse response = UserReservationResponse.of("보예",
            List.of(Reservation.of(1L, ReservationDate.of(1L, LocalDate.of(2026, 5, 17)),
                    ReservationTime.of(1L, LocalTime.of(10, 10)),
                    Theme.of(1L, "공포", "아무서워", "theme-url")
                )
            )
        );
        given(reservationService.getUserReservations(name)).willReturn(response);

        // when & then
        mockMvc.perform(get("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", name))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("보예"))
            .andExpect(jsonPath("$.reservation[0].reservationId").value(1L))
            .andExpect(jsonPath("$.reservation[0].date.startWhen").value("2026-05-17"))
            .andExpect(jsonPath("$.reservation[0].time.startAt").value("10:10"))
            .andExpect(jsonPath("$.reservation[0].theme.name").value("공포"))
            .andExpect(jsonPath("$.reservation[0].theme.content").value("아무서워"))
            .andExpect(jsonPath("$.reservation[0].theme.url").value("theme-url"));
    }

    @Test
    @DisplayName("예약자 이름 조회 시 이름이 누락되면 예외가 발생한다.")
    void getUserReservationsWithoutName() throws Exception {
        // given & when & then
        mockMvc.perform(get("/reservations")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUIRED_PARAMETER_MISSING"))
            .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 삭제의 정상 요청과 응답을 확인한다.")
    void cancelUserReservation() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id))
            .andExpect(status().isNoContent());

        verify(reservationService).cancelUserReservation(id);
    }

    @Test
    @DisplayName("예약 수정의 정상 요청과 응답을 확인한다.")
    void updateReservation() throws Exception {
        // given
        Long id = 1L;
        UpdateReservationRequest request = new UpdateReservationRequest(
            LocalDate.of(2026, 5, 18),
            LocalTime.of(14, 30)
        );

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(reservationService).updateReservation(id, request);
    }

    @Test
    @DisplayName("예약 수정 시 잘못된 요청 형식이면 예외가 발생한다.")
    void updateReservationWithInvalidFormat() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "startWhen": "2026/05/18",
                      "startAt": "14:30"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INPUT_FORMAT_ERROR"))
            .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다. 날짜는 yyyy-MM-dd, 시간은 HH:mm 형식으로 입력해주세요."));
    }

    @Test
    @DisplayName("수정할 예약이 없으면 예외가 발생한다.")
    void updateReservationWhenReservationNotFound() throws Exception {
        // given
        Long id = 999L;
        UpdateReservationRequest request = new UpdateReservationRequest(
            LocalDate.of(2026, 5, 18),
            LocalTime.of(14, 30)
        );
        willThrow(new NotFoundException(ReservationErrors.RESERVATION_NOT_FOUND))
            .given(reservationService)
            .updateReservation(id, request);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 예약건 입니다"));
    }

    @Test
    @DisplayName("예약 삭제 시 id를 누락한 경우 예외가 발생한다.")
    void cancelUserReservationWithoutId() throws Exception {
        // given & when & then
        mockMvc.perform(delete("/reservations"))
            .andExpect(status().isMethodNotAllowed());
    }
}
