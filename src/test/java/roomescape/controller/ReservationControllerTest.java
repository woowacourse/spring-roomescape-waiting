package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.controller.dto.DisplayStatus;
import roomescape.controller.dto.ReservationResponse;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.global.DomainErrorHttpMapper;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
@Import(DomainErrorHttpMapper.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @DisplayName("사용자 예약 생성 요청은 201과 Location 헤더를 반환한다.")
    @Test
    void create() throws Exception {
        given(reservationService.saveReservation(any())).willReturn(1L);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "러로",
                                  "date": "2026-07-01",
                                  "timeId": 1,
                                  "themeId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"));
    }

    @DisplayName("예약 생성 요청 값이 올바르지 않으면 400을 반환한다.")
    @Test
    void createInvalidRequest() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "date": "2026-07-01",
                                  "timeId": 1,
                                  "themeId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @DisplayName("사용자 예약 목록을 JSON으로 반환한다.")
    @Test
    void findByName() throws Exception {
        given(reservationService.findByName("러로")).willReturn(List.of(
                new ReservationResponse(
                        1L,
                        "러로",
                        DisplayStatus.WAITING,
                        LocalDate.of(2026, 7, 1),
                        "잠긴 방",
                        "닫힌 문을 여는 테마",
                        "https://example.com/theme.jpg",
                        LocalTime.of(10, 0),
                        1
                )
        ));

        mockMvc.perform(get("/reservations").param("name", "러로"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].name").value("러로"))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].date").value("2026-07-01"))
                .andExpect(jsonPath("$[0].themeName").value("잠긴 방"))
                .andExpect(jsonPath("$[0].time").value("10:00"))
                .andExpect(jsonPath("$[0].order").value(1));
    }

    @DisplayName("사용자는 이름을 함께 보내 예약을 취소한다.")
    @Test
    void cancel() throws Exception {
        mockMvc.perform(delete("/reservations/1").param("name", "러로"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L, "러로");
    }

    @DisplayName("본인 예약이 아니면 403을 반환한다.")
    @Test
    void cancelUnauthorized() throws Exception {
        org.mockito.Mockito.doThrow(new RoomescapeException(
                        DomainErrorCode.UNAUTHORIZED_RESERVATION,
                        "본인의 예약만 변경할 수 있습니다."
                ))
                .when(reservationService)
                .cancelReservation(1L, "다른사람");

        mockMvc.perform(delete("/reservations/1").param("name", "다른사람"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_RESERVATION"));
    }

    @DisplayName("예약 수정 요청은 서비스에 위임하고 200을 반환한다.")
    @Test
    void update() throws Exception {
        mockMvc.perform(patch("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "러로",
                                  "date": "2026-07-02",
                                  "timeId": 1,
                                  "themeId": 1
                                }
                                """))
                .andExpect(status().isOk());

        verify(reservationService).updateReservation(eq(1L), any());
    }
}
