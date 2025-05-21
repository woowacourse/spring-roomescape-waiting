package roomescape.unit.reservation.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.presentation.AuthorizationExtractor;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationWithStatusResponse;
import roomescape.reservation.dto.response.TimeSlotResponse;
import roomescape.reservation.presentation.ReservationController;
import roomescape.reservation.service.ReservationService;

@WebMvcTest(value = {ReservationController.class, AuthorizationExtractor.class})
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @Test
    void 사용자가_예약을_생성한다() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 1, 1), 1L, 1L);
        ReservationResponse response = new ReservationResponse(1L, "memberName1", LocalDate.of(2025, 1, 1),
                new TimeSlotResponse(1L, LocalTime.of(9, 0)), "themeName1");
        given(reservationService.createReservation(1L, request.timeId(), request.themeId(), request.date())).willReturn(
                response);
        given(tokenProvider.extractSubject("accessToken")).willReturn("1");
        // when & then
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("token", "accessToken")))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 사용자가_예약을_조회한다() throws Exception {
        // given

        ReservationWithStatusResponse response = new ReservationWithStatusResponse(1L, "memberName1",
                LocalDate.of(2025, 1, 1),
                new TimeSlotResponse(1L, LocalTime.of(9, 0)), "themeName1", "예약");

        given(reservationService.findReservationByMemberId(1L)).willReturn(List.of(response));
        given(tokenProvider.extractSubject("accessToken")).willReturn("1");
        // when & then

        mockMvc.perform(get("/api/reservations/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", "accessToken")))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(response))));
    }

    @Test
    void 예약을_삭제하는데_성공한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/reservations/{reservationId}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


}