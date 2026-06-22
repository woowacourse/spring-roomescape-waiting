package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void 전체_예약_목록_조회_요청을_Service에_전달하고_결과를_반환한다() throws Exception {
        List<Reservation> reservations = List.of(
                new Reservation(1L, "레서", LocalDate.of(2026, 5, 6),
                        new ReservationTime(1L, LocalTime.of(18, 0)),
                        new Theme(1L, "공포방", "무서운방입니다.", "image-url"),
                        ReservationStatus.CONFIRMED, null, null),
                new Reservation(2L, "어셔", LocalDate.of(2026, 5, 7),
                        new ReservationTime(2L, LocalTime.of(20, 0)),
                        new Theme(2L, "추리방", "추리하는방입니다.", "image-url2"),
                        ReservationStatus.CONFIRMED, null, null)
        );
        when(reservationService.findReservations(1, 5)).thenReturn(reservations);

        mockMvc.perform(get("/admin/reservations")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(2))
                .andExpect(jsonPath("$.reservations[0].id").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("레서"))
                .andExpect(jsonPath("$.reservations[1].name").value("어셔"));

        verify(reservationService).findReservations(1, 5);
    }

    @Test
    void 전체_예약_목록_조회시_기본_페이지_정보를_Service에_전달한다() throws Exception {
        when(reservationService.findReservations(0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(0));

        verify(reservationService).findReservations(0, 10);
    }

    @Test
    void 시간_생성_요청을_받으면_DTO의_시작_시간을_Service에_전달하고_결과를_반환한다() throws Exception {
        ReservationTime created = new ReservationTime(1L, LocalTime.of(18, 0));

        when(reservationTimeService.createTime(any())).thenReturn(created);

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "18:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.startAt").value("18:00"));
    }

    @Test
    void 시간_삭제_요청을_받으면_PathVariable_id를_Service에_전달한다() throws Exception {
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 테마_생성_요청을_받으면_DTO의_테마명_설명_이미지주소를_Service에_전달하고_결과를_반환한다() throws Exception {
        Theme created = new Theme(1L, "공포방", "무서운방입니다.", "image-url");

        when(themeService.createTheme(any(), any(), any())).thenReturn(created);
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "name": "공포방",
                                    "description": "무서운방입니다.",
                                    "thumbnail": "image-url"
                                  }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("공포방"))
                .andExpect(jsonPath("$.description").value("무서운방입니다."))
                .andExpect(jsonPath("$.thumbnail").value("image-url"));

    }

    @Test
    void 테마_삭제_요청을_받으면_PathVariable_id를_Service에_전달한다() throws Exception {
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNoContent());
    }
}
