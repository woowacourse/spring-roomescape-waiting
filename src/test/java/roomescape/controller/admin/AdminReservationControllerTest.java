package roomescape.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 예약_목록을_조회한다() throws Exception {
        // given
        given(reservationService.findAll())
                .willReturn(List.of(reservation()));

        // when & then
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].theme.id").value(1))
                .andExpect(jsonPath("$[0].theme.name").value("테마"));

        verify(reservationService, times(1)).findAll();
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 관리자_예약을_생성한다() throws Exception {
        // given
        given(reservationService.createByAdmin(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willReturn(reservation());

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/admin/reservations/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"));

        verify(reservationService, times(1)).createByAdmin(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 중복_예약이면_에러_응답() throws Exception {
        // given
        given(reservationService.createByAdmin(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약된 시간입니다."));

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"))
                .andExpect(jsonPath("$.detail").value("이미 예약된 시간입니다."));

        verify(reservationService, times(1)).createByAdmin(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 예약을_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/reservations/1"))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).deleteByAdmin(1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 삭제_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/reservations/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationService);
    }

    private String validRequest() {
        return """
                {
                  "name": "브라운",
                  "date": "2099-01-01",
                  "timeId": 1,
                  "themeId": 1
                }
                """;
    }

    private Reservation reservation() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(1L, "브라운", LocalDate.of(2099, 1, 1), time, theme);
    }
}
