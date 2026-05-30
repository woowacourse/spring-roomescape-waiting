package roomescape.ui.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.holiday.service.HolidayService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.TimeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RoomescapePageController.class)
class RoomescapePageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private TimeService timeService;

    @MockitoBean
    private HolidayService holidayService;

    @DisplayName("대시보드 예약 페이지는 reservations, themes, times를 모델에 담아 dashboard/reservations 뷰를 반환한다")
    @Test
    void dashboardReservationsPage_모델_조립_검증() throws Exception {
        when(reservationService.getAll()).thenReturn(List.of());
        when(themeService.getAll()).thenReturn(List.of());
        when(timeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/page/dashboard/reservations"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/reservations"))
                .andExpect(model().attributeExists("reservations"))
                .andExpect(model().attributeExists("themes"))
                .andExpect(model().attributeExists("times"));
    }

    @DisplayName("사용자 예약 페이지는 name 파라미터가 있으면 reservations와 name을 모델에 담는다")
    @Test
    void userReservationsPage_name_있으면_reservations_모델에_담김() throws Exception {
        when(themeService.getAll()).thenReturn(List.of());
        when(timeService.findAll()).thenReturn(List.of());
        when(reservationService.getAllByName("라이")).thenReturn(List.of());

        mockMvc.perform(get("/page/reservations").param("name", "라이"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations"))
                .andExpect(model().attribute("name", "라이"))
                .andExpect(model().attributeExists("reservations"));
    }

    @DisplayName("사용자 예약 페이지는 name 파라미터가 없으면 reservations 모델 속성을 두지 않는다")
    @Test
    void userReservationsPage_name_없으면_reservations_없음() throws Exception {
        when(themeService.getAll()).thenReturn(List.of());
        when(timeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/page/reservations"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations"))
                .andExpect(model().attributeDoesNotExist("reservations"));
    }

    @DisplayName("사용자 예약 생성 - RESERVED로 저장되면 '예약 완료' 메시지를 flash에 담는다")
    @Test
    void createUserReservation_RESERVED_성공_메시지() throws Exception {
        Reservation reserved = newReservation(Status.RESERVED);
        ReservationSaveServiceRequest expected = new ReservationSaveServiceRequest("라이", 1L, 1L);
        when(reservationService.create(expected)).thenReturn(reserved);

        mockMvc.perform(post("/page/reservations")
                        .param("name", "라이")
                        .param("themeId", "1")
                        .param("timeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/page/reservations?name=라이"))
                .andExpect(flash().attribute("successMessage", "예약이 완료되었습니다."));
    }

    @DisplayName("사용자 예약 생성 - WAITING으로 저장되면 '대기 등록' 메시지를 flash에 담는다")
    @Test
    void createUserReservation_WAITING_대기_메시지() throws Exception {
        Reservation waiting = newReservation(Status.WAITING);
        ReservationSaveServiceRequest expected = new ReservationSaveServiceRequest("라이", 1L, 1L);
        when(reservationService.create(expected)).thenReturn(waiting);

        mockMvc.perform(post("/page/reservations")
                        .param("name", "라이")
                        .param("themeId", "1")
                        .param("timeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/page/reservations?name=라이"))
                .andExpect(flash().attribute("successMessage", "이미 예약된 슬롯이라 예약 대기로 등록되었습니다."));
    }

    @DisplayName("사용자 예약 생성 - 비즈니스 예외 발생 시 에러 메시지를 flash에 담는다")
    @Test
    void createUserReservation_예외_시_에러_메시지() throws Exception {
        ReservationSaveServiceRequest expected = new ReservationSaveServiceRequest("라이", 1L, 1L);
        when(reservationService.create(expected)).thenThrow(new DuplicateReservationException());

        mockMvc.perform(post("/page/reservations")
                        .param("name", "라이")
                        .param("themeId", "1")
                        .param("timeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/page/reservations?name=라이"))
                .andExpect(flash().attribute("errorMessage", "예약 생성에 실패했습니다. 입력값을 다시 확인해 주세요."));
    }

    @DisplayName("사용자 예약 취소 - WAITING 취소 시 '예약 대기 취소' 메시지를 flash에 담는다")
    @Test
    void cancelUserReservation_WAITING_대기_취소_메시지() throws Exception {
        mockMvc.perform(post("/page/reservations/{id}/cancel", 1L)
                        .param("name", "라이")
                        .param("status", "WAITING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/page/reservations?name=라이"))
                .andExpect(flash().attribute("successMessage", "예약 대기를 취소했습니다."));

        verify(reservationService).cancelForUser(1L, "라이");
    }

    @DisplayName("사용자 예약 취소 - RESERVED 취소 시 '예약 취소' 메시지를 flash에 담는다")
    @Test
    void cancelUserReservation_RESERVED_취소_메시지() throws Exception {
        mockMvc.perform(post("/page/reservations/{id}/cancel", 1L)
                        .param("name", "라이")
                        .param("status", "RESERVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/page/reservations?name=라이"))
                .andExpect(flash().attribute("successMessage", "예약을 취소했습니다."));

        verify(reservationService).cancelForUser(1L, "라이");
    }

    private Reservation newReservation(Status status) {
        ReservationTime time = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        return new Reservation("라이", time, theme, status, LocalDateTime.now()).withId(1L);
    }
}
