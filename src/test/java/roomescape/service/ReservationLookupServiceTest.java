package roomescape.service;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.ReservationStatus;
import roomescape.service.dto.Status;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationLookupServiceTest {

    private final ReservationService reservationService = mock();
    private final ReservationWaitingService reservationWaitingService = mock();
    private final ReservationLookupService service = new ReservationLookupService(
            reservationService,
            reservationWaitingService);

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");

    @Test
    void 이름으로_예약과_예약_대기를_함께_조회한다() {
        // given
        String name = "브라운";
        Reservation reservation = new Reservation(1L, name, date, time, theme);
        WaitingResult waiting = new WaitingResult(2L, name, date.plusDays(1), time, theme, 1L);

        when(reservationService.findByName(name))
                .thenReturn(List.of(reservation));
        when(reservationWaitingService.findByName(name))
                .thenReturn(List.of(waiting));

        // when
        List<ReservationStatus> result = service.findByName(name);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(ReservationStatus::id)
                        .containsExactly(2L, 1L),
                () -> assertThat(result).extracting(ReservationStatus::status)
                        .containsExactly(Status.WAITING, Status.RESERVED),
                () -> assertThat(result).extracting(ReservationStatus::turn)
                        .containsExactly(1L, null));
    }

    @Test
    void 예약과_예약_대기를_날짜와_시간_내림차순으로_조회한다() {
        // given
        String name = "브라운";
        ReservationTime earlyTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        ReservationTime lateTime = new ReservationTime(2L, LocalTime.parse("12:00"));

        Reservation earlyReservation = new Reservation(1L, name, date, earlyTime, theme);
        Reservation lateReservation = new Reservation(2L, name, date, lateTime, theme);
        WaitingResult futureWaiting = new WaitingResult(3L, name, date.plusDays(1), earlyTime, theme, 1L);

        when(reservationService.findByName(name))
                .thenReturn(List.of(earlyReservation, lateReservation));
        when(reservationWaitingService.findByName(name))
                .thenReturn(List.of(futureWaiting));

        // when
        List<ReservationStatus> result = service.findByName(name);

        // then
        assertThat(result).extracting(ReservationStatus::id)
                .containsExactly(3L, 2L, 1L);
    }
}
