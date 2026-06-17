package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.dto.UserReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 내_예약_목록_조회_요청을_Service에_전달하고_결과를_반환한다() throws Exception {
        List<UserReservation> reservations = List.of(
                new UserReservation(new Reservation(1L, "레서", new Schedule(LocalDate.of(2026, 5, 6),
                        new ReservationTime(1L, LocalTime.of(18, 0)),
                        new Theme(1L, "공포방", "무서운방입니다.", "image-url"))), null),

                new UserReservation(new Reservation(2L, "레서", new Schedule(LocalDate.of(2026, 5, 7),
                        new ReservationTime(2L, LocalTime.of(20, 0)),
                        new Theme(2L, "추리방", "추리하는방입니다.", "image-url2"))), null)
        );
        when(reservationService.findUserReservationsWithPayments("레서", 1, 5)).thenReturn(reservations);

        mockMvc.perform(get("/reservations")
                        .param("name", "레서")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(2))
                .andExpect(jsonPath("$.reservations[0].id").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("레서"))
                .andExpect(jsonPath("$.reservations[0].date").value("2026-05-06"))
                .andExpect(jsonPath("$.reservations[0].time.startAt").value("18:00"))
                .andExpect(jsonPath("$.reservations[0].theme.name").value("공포방"))
                .andExpect(jsonPath("$.reservations[1].id").value(2))
                .andExpect(jsonPath("$.reservations[1].date").value("2026-05-07"));

        verify(reservationService).findUserReservationsWithPayments("레서", 1, 5);
    }

    @Test
    void 내_예약_목록_조회시_이름_파라미터가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_생성_요청을_받으면_DTO의_이름_날짜_시간_id_테마_id를_Service에_전달하고_결과를_반환한다() throws Exception {
        Reservation created = new Reservation(1L, "레서", new Schedule(LocalDate.of(2026, 5, 6),
                new ReservationTime(1L, LocalTime.of(18,0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")));

        when(reservationService.createReservation(any(), any(), anyLong(), anyLong())).thenReturn(created);
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "name": "레서",
                                    "date": "2026-05-06",
                                    "timeId": 1,
                                    "themeId": 1
                                  }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("레서"))
                .andExpect(jsonPath("$.date").value("2026-05-06"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("18:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("공포방"))
                .andExpect(jsonPath("$.theme.description").value("무서운방입니다."))
                .andExpect(jsonPath("$.theme.thumbnail").value("image-url"));
    }

    @Test
    void 예약_변경_요청을_받으면_id와_이름_날짜_시간_id를_Service에_전달하고_결과를_반환한다() throws Exception {
        Reservation updated = new Reservation(1L, "레서", new Schedule(LocalDate.of(2026, 5, 7),
                new ReservationTime(2L, LocalTime.of(20, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")));

        when(reservationService.updateReservation(anyLong(), any(), any(), anyLong())).thenReturn(updated);

        mockMvc.perform(patch("/reservations/1")
                        .param("name", "레서")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "date": "2026-05-07",
                                    "timeId": 2
                                  }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("레서"))
                .andExpect(jsonPath("$.date").value("2026-05-07"))
                .andExpect(jsonPath("$.time.id").value(2))
                .andExpect(jsonPath("$.time.startAt").value("20:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("공포방"));

        verify(reservationService).updateReservation(1L, "레서", LocalDate.of(2026, 5, 7), 2L);
    }

    @Test
    void 사용자_예약_삭제_요청을_받으면_PathVariable_id와_이름을_Service에_전달한다() throws Exception {
        mockMvc.perform(delete("/reservations/1")
                        .param("name", "레서"))
                .andExpect(status().isNoContent());
        verify(reservationService, times(1)).deleteUserReservation(1L, "레서");
    }
}
