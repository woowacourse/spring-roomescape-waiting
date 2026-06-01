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
                        .containsExactly(1L, 2L),
                () -> assertThat(result).extracting(ReservationStatus::status)
                        .containsExactly(Status.RESERVED, Status.WAITING),
                () -> assertThat(result).extracting(ReservationStatus::turn)
                        .containsExactly(null, 1L));
    }
}
