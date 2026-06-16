package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;
import roomescape.repository.theme.ThemeRepository;

@SpringBootTest
class JdbcReservationWaitingRepositoryTest {

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
    }

    @Test
    @DisplayName("예약 대기를 저장한다")
    void save() {
        Reservation reservation = createReservation();
        ReservationWaiting waiting = ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        );

        ReservationWaiting saved = reservationWaitingRepository.save(waiting);

        assertThat(saved.getId()).isEqualTo(1L);
        Long slotId = jdbcTemplate.queryForObject(
                "SELECT slot_id FROM reservation_waiting WHERE id = ?",
                Long.class,
                saved.getId()
        );
        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM reservation_waiting WHERE id = ?",
                String.class,
                saved.getId()
        );

        assertThat(slotId).isEqualTo(reservation.getSlot().getId());
        assertThat(name).isEqualTo("아루");
    }

    @Test
    @DisplayName("같은 슬롯에 같은 이름으로 중복 대기를 저장할 수 없다")
    void saveDuplicateNameForSlot() {
        Reservation reservation = createReservation();

        reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        assertThrows(PersistenceConflictException.class, () -> reservationWaitingRepository.save(
                ReservationWaiting.createNew(reservation.getSlot(), "아루", LocalDateTime.parse("2026-08-05T12:01:00"))
        ));
    }

    @Test
    @DisplayName("대기 ID로 예약 대기를 조회한다")
    void findById() {
        Reservation reservation = createReservation();
        ReservationWaiting saved = reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        ReservationWaiting found = reservationWaitingRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(found).isEqualTo(saved);
        assertThat(found.getName()).isEqualTo("아루");
        assertThat(found.getSlot().getId()).isEqualTo(reservation.getSlot().getId());
        assertThat(found.getRequestedAt()).isEqualTo(LocalDateTime.parse("2026-08-05T12:00:00"));
    }

    @Test
    @DisplayName("대기 ID로 예약 대기를 삭제한다")
    void deleteById() {
        Reservation reservation = createReservation();
        ReservationWaiting saved = reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        reservationWaitingRepository.delete(saved);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation_waiting WHERE id = ?",
                Integer.class,
                saved.getId()
        );

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("슬롯의 대기 줄을 조회한다")
    void findLineBySlot() {
        Reservation reservation = createReservation();
        ReservationWaiting saved = reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        ReservationWaitingLine waitingLine = reservationWaitingRepository.findLineBySlot(reservation.getSlot());

        assertThat(waitingLine.isEmpty()).isFalse();
        assertThat(waitingLine.indexOf(saved.getId())).hasValue(0);
    }

    @Test
    @DisplayName("대기 줄에서 첫 번째 대기를 찾는다")
    void findLineBySlotFirst() {
        Reservation reservation = createReservation();
        ReservationWaiting second = reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "도기",
                LocalDateTime.parse("2026-08-05T12:01:00")
        ));
        ReservationWaiting first = reservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        ReservationWaiting found = reservationWaitingRepository.findLineBySlot(reservation.getSlot())
                .first()
                .orElseThrow();

        assertThat(found).isEqualTo(first);
        assertThat(found).isNotEqualTo(second);
    }

    private Reservation createReservation() {
        Theme theme = themeRepository.save(
                Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
        );
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.createNew(LocalTime.parse("10:00"))
        );

        ReservationSlot slot = reservationSlotRepository.save(
                new ReservationSlot(LocalDate.parse("2026-08-06"), theme, reservationTime)
        );
        return reservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_slot");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_slot ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
