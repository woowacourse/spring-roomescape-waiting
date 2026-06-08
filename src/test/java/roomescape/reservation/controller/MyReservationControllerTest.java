package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationUpdateCommand;

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
    @DisplayName("인증 헤더가 없으면 예약 수정 시 401을 반환한다.")
    void updateMyReservation_MissingAuthHeader_Unauthorized() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(1), 1L);

        mockMvc.perform(patch("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("예약 소유자가 불일치하면 403을 반환한다.")
    void updateMyReservation_MismatchOwner_Forbidden() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(1), 1L);
        willThrow(new ForbiddenException("접근 권한이 없습니다."))
                .given(reservationService).update(any(ReservationUpdateCommand.class), any());

        mockMvc.perform(patch("/reservations/1")
                        .header("Authorization", "다른사용자")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("예약 수정 시 date와 timeId가 모두 null이면 400을 반환한다.")
    void updateMyReservation_BothNullFields_BadRequest() throws Exception {
        mockMvc.perform(patch("/reservations/1")
                        .header("Authorization", "브라운")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 수정 요청이 성공하면 204를 반환한다.")
    void updateMyReservation_Success() throws Exception {
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.now().plusDays(1), null);

        mockMvc.perform(patch("/reservations/1")
                        .header("Authorization", "브라운")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예약 삭제 시 401을 반환한다.")
    void deleteMyReservation_MissingAuthHeader_Unauthorized() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 예약 삭제가 성공하면 204를 반환한다.")
    void deleteMyReservation_Success() throws Exception {
        mockMvc.perform(delete("/reservations/1")
                        .header("Authorization", "브라운"))
                .andExpect(status().isNoContent());
    }
}
