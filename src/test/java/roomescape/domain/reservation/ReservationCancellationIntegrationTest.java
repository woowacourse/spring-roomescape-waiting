package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql("/truncate.sql")
class ReservationCancellationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Slot cancelledSlot;

    @BeforeEach
    void setUp() {
        cancelledSlot = insertSlot(
            101L, LocalDate.now().plusDays(2),
            201L, LocalTime.of(10, 0),
            301L, "공포"
        );
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
        Slot otherSlot = insertSlot(
            102L, LocalDate.now().plusDays(3),
            202L, LocalTime.of(11, 0),
            302L, "스릴러"
        );
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

    @Test
    void 사용자가_본인의_예약을_수정하면_기존_슬롯의_1순위_대기가_예약으로_변경된다() {
        Reservation updatedReservation = reservationRepository.save(
            Reservation.createWithoutId(
                "테스터",
                cancelledSlot.date(),
                cancelledSlot.time(),
                cancelledSlot.theme()
            )
        );
        Slot newSlot = insertSlot(
            102L, LocalDate.now().plusDays(3),
            202L, LocalTime.of(11, 0),
            302L, "스릴러"
        );
        WaitingReservation firstWaiting = waitingReservationRepository.save(
            waiting("이산", cancelledSlot, LocalDateTime.of(2026, 5, 6, 10, 0))
        );
        WaitingReservation secondWaiting = waitingReservationRepository.save(
            waiting("고래", cancelledSlot, LocalDateTime.of(2026, 5, 7, 10, 0))
        );

        reservationService.updateReservation(
            updatedReservation.getId(),
            new ReservationUpdateRequest(
                newSlot.date().getId(),
                newSlot.time().getId()
            )
        );

        Reservation movedReservation = reservationRepository.findById(updatedReservation.getId()).orElseThrow();
        Reservation promotedReservation = reservationRepository.findByName("이산").get(0);
        assertThat(movedReservation.getDate().getId()).isEqualTo(newSlot.date().getId());
        assertThat(movedReservation.getTime().getId()).isEqualTo(newSlot.time().getId());
        assertThat(promotedReservation.getDate().getId()).isEqualTo(cancelledSlot.date().getId());
        assertThat(promotedReservation.getTime().getId()).isEqualTo(cancelledSlot.time().getId());
        assertThat(waitingReservationRepository.findById(firstWaiting.getId())).isEmpty();
        assertThat(waitingReservationRepository.findById(secondWaiting.getId())).isPresent();
    }

    private WaitingReservation waiting(String name, Slot slot, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    private Slot insertSlot(long dateId, LocalDate playDay, long timeId, LocalTime startAt, long themeId,
        String themeName) {
        jdbcTemplate.update("insert into reservation_date(id, play_day) values (?, ?)", dateId, playDay.toString());
        jdbcTemplate.update("insert into reservation_time(id, start_at) values (?, ?)", timeId, startAt.toString());
        jdbcTemplate.update(
            "insert into theme(id, name, content, url) values (?, ?, ?, ?)",
            themeId, themeName, "테마 내용", "/themes/" + themeId
        );
        return new Slot(
            ReservationDate.of(dateId, playDay),
            ReservationTime.of(timeId, startAt),
            Theme.of(themeId, themeName, "테마 내용", "/themes/" + themeId)
        );
    }

    private record Slot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
    }
}
