package roomescape.controller.admin;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.service.BookingLookupService;
import roomescape.service.dto.BookingStatus;
import roomescape.service.dto.BookingType;

@WebMvcTest(AdminBookingController.class)
class AdminBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingLookupService bookingLookupService;

    @Test
    void 기간으로_예약과_예약_대기를_함께_조회한다() throws Exception {
        LocalDate startDate = LocalDate.of(2023, 8, 1);
        LocalDate endDate = LocalDate.of(2023, 8, 31);
        given(bookingLookupService.findByDateRange(eq(startDate), eq(endDate)))
                .willReturn(List.of(reservationBooking(), waitingBooking()));

        mockMvc.perform(get("/admin/bookings")
                        .param("startDate", "2023-08-01")
                        .param("endDate", "2023-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].theme.id").value(1))
                .andExpect(jsonPath("$[0].theme.name").value("테마"))
                .andExpect(jsonPath("$[0].bookingType").value("RESERVATION"))
                .andExpect(jsonPath("$[0].reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].turn").value(nullValue()))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].bookingType").value("WAITING"))
                .andExpect(jsonPath("$[1].reservationStatus").value(nullValue()))
                .andExpect(jsonPath("$[1].turn").value(1));

        verify(bookingLookupService, times(1)).findByDateRange(startDate, endDate);
        verifyNoMoreInteractions(bookingLookupService);
    }

    @Test
    void 시작일이_없으면_에러_응답() throws Exception {
        mockMvc.perform(get("/admin/bookings").param("endDate", "2023-08-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("startDate는 필수입니다."));

        verifyNoMoreInteractions(bookingLookupService);
    }

    @Test
    void 종료일_형식이_올바르지_않으면_에러_응답() throws Exception {
        mockMvc.perform(get("/admin/bookings")
                        .param("startDate", "2023-08-01")
                        .param("endDate", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("endDate 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(bookingLookupService);
    }

    private BookingStatus reservationBooking() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new BookingStatus(1L, "브라운", LocalDate.of(2099, 1, 1), time, theme,
                BookingType.RESERVATION, ReservationStatus.CONFIRMED, null);
    }

    private BookingStatus waitingBooking() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new BookingStatus(2L, "구구", LocalDate.of(2099, 1, 1), time, theme,
                BookingType.WAITING, null, 1L);
    }
}
