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

@WebMvcTest(AdminReservationController.class)
@Import(DomainErrorHttpMapper.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @DisplayName("관리자는 모든 예약 목록을 조회한다.")
    @Test
    void findAll() throws Exception {
        given(reservationService.findAll()).willReturn(List.of(
                new ReservationResponse(
                        1L,
                        "러로",
                        DisplayStatus.RESERVED,
                        LocalDate.of(2026, 7, 1),
                        "잠긴 방",
                        "설명",
                        "https://example.com/theme.jpg",
                        LocalTime.of(10, 0),
                        0
                )
        ));

        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(1))
                .andExpect(jsonPath("$[0].status").value("RESERVED"))
                .andExpect(jsonPath("$[0].time").value("10:00"));
    }

    @DisplayName("관리자 예약 생성은 201과 Location 헤더를 반환한다.")
    @Test
    void create() throws Exception {
        given(reservationService.saveReservation(any())).willReturn(1L);

        mockMvc.perform(post("/admin/reservations")
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

    @DisplayName("관리자 예약 수정은 서비스에 위임한다.")
    @Test
    void update() throws Exception {
        mockMvc.perform(patch("/admin/reservations/1")
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

    @DisplayName("관리자 예약 취소는 이름 파라미터와 함께 서비스에 위임한다.")
    @Test
    void cancel() throws Exception {
        mockMvc.perform(delete("/admin/reservations/1").param("name", "러로"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L, "러로");
    }

    @DisplayName("존재하지 않는 예약이면 404를 반환한다.")
    @Test
    void cancelNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new RoomescapeException(
                        DomainErrorCode.NOT_FOUND_RESERVATION,
                        "해당 ID의 예약이 존재하지 않습니다."
                ))
                .when(reservationService)
                .cancelReservation(404L, "러로");

        mockMvc.perform(delete("/admin/reservations/404").param("name", "러로"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_RESERVATION"));
    }
}
