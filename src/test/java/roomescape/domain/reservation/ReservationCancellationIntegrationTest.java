package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationCancellationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Slot cancelledSlot;

    @BeforeEach
    void setUp() {
        cancelledSlot = insertSlot(LocalDate.now().plusDays(2), LocalTime.of(10, 0), "공포");
    }

    @Test
    void 사용자가_본인의_예약을_취소하면_같은_슬롯의_1순위_대기가_예약으로_변경된다() {
        Reservation cancelledReservation = reservationRepository.save(
            Reservation.createWithoutId(
                "테스터",
                cancelledSlot.date(),
                cancelledSlot.time(),
                cancelledSlot.theme()
            )
        );
        Slot otherSlot = insertSlot(LocalDate.now().plusDays(3), LocalTime.of(11, 0), "스릴러");
        WaitingReservation otherSlotOldest = waitingReservationRepository.save(
            waiting("다른슬롯", otherSlot, LocalDateTime.of(2026, 5, 5, 10, 0))
        );
        WaitingReservation firstWaiting = waitingReservationRepository.save(
            waiting("이산", cancelledSlot, LocalDateTime.of(2026, 5, 6, 10, 0))
        );
        WaitingReservation secondWaiting = waitingReservationRepository.save(
            waiting("고래", cancelledSlot, LocalDateTime.of(2026, 5, 7, 10, 0))
        );

        reservationService.cancelReservation(cancelledReservation.getId());

        assertThat(reservationRepository.findById(cancelledReservation.getId())).isEmpty();
        assertThat(reservationRepository.findByName("이산")).hasSize(1);
        assertThat(reservationRepository.findByName("다른슬롯")).isEmpty();
        assertThat(waitingReservationRepository.findById(firstWaiting.getId())).isEmpty();
        assertThat(waitingReservationRepository.findById(secondWaiting.getId())).isPresent();
        assertThat(waitingReservationRepository.findById(otherSlotOldest.getId())).isPresent();
    }

    private WaitingReservation waiting(String name, Slot slot, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    private Slot insertSlot(LocalDate playDay, LocalTime startAt, String themeName) {
        ReservationDate date = reservationDateRepository.save(ReservationDate.createWithoutId(playDay));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));
        Theme theme = themeRepository.save(Theme.createWithoutId(themeName, "테마 내용", "/themes/" + themeName));
        return new Slot(date, time, theme);
    }

    private record Slot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
    }
}
