package roomescape.reservation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthInterceptor;
import roomescape.auth.OwnerOnlyArgumentResolver;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.exception.ForbiddenException;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@WebMvcTest(MyReservationController.class)
@Import({WebMvcConfig.class, AuthInterceptor.class, OwnerOnlyArgumentResolver.class})
class MyReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("인증 헤더가 없으면 예약 수정 시 401 에러를 반환한다.")
    void updateMyReservation_MissingAuthHeader_Unauthorized() throws Exception {
        // given
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 5, 5), 1L);

        // when & then
        mockMvc.perform(patch("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증 정보가 만료되었거나 없습니다. 다시 로그인한 후 시도해 주세요."));
    }

    @Test
    @DisplayName("예약 소유자가 불일치하면 403 에러를 반환한다.")
    void updateMyReservation_MismatchOwner_Forbidden() throws Exception {
        // given
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 5, 5), 1L);
        willThrow(new ForbiddenException("접근 권한이 없습니다."))
                .given(reservationService).validateOwnership(1L, "다른사용자");

        // when & then
        mockMvc.perform(patch("/reservations/1")
                        .header("Authorization", "다른사용자")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("예약 수정 시 date와 timeId가 모두 null이면 400 에러를 반환한다.")
    void updateMyReservation_BothNullFields_BadRequest() throws Exception {
        // given
        String requestBody = "{}";

        // when & then
        mockMvc.perform(patch("/reservations/1")
                        .header("Authorization", "브라운")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다. 안내된 양식에 맞춰 다시 입력해 주세요."));
    }

    @Test
    @DisplayName("예약 수정 요청이 성공하면 204 No Content를 반환한다.")
    void updateMyReservation_Success() throws Exception {
        // given
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 5, 5), null);

        // when & then
        mockMvc.perform(patch("/reservations/1")
                        .header("Authorization", "브라운")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예약 삭제 시 401 에러를 반환한다.")
    void deleteMyReservation_MissingAuthHeader_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증 정보가 만료되었거나 없습니다. 다시 로그인한 후 시도해 주세요."));
    }

    @Test
    @DisplayName("내 예약을 성공적으로 삭제하면 204 No Content를 반환한다.")
    void deleteMyReservation_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/1")
                        .header("Authorization", "브라운"))
                .andExpect(status().isNoContent());
    }
}
