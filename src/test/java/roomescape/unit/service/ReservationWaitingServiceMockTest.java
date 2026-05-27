package roomescape.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.ReservationWaitingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceMockTest {

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Test
    void addWaiting은_저장소에_위임하고_저장된_대기를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
        Reservation reservation = new Reservation(1L, "티뉴", LocalDate.of(2026, 8, 5), time, theme);
        ReservationWaiting toSave = new ReservationWaiting("민욱", LocalDateTime.of(2026, 8, 1, 10, 0), reservation);
        ReservationWaiting saved = new ReservationWaiting(1L, "민욱", toSave.getCreatedAt(), reservation, 1);
        given(reservationWaitingRepository.save(toSave)).willReturn(saved);

        assertThat(reservationWaitingService.addWaiting(toSave)).isEqualTo(saved);
    }
}
