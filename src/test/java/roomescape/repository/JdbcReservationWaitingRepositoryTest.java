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
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservation.JdbcReservationRepository;
import roomescape.repository.reservationtime.JdbcReservationTimeRepository;
import roomescape.repository.reservationslot.JdbcReservationSlotRepository;
import roomescape.repository.reservationwaiting.JdbcReservationWaitingRepository;
import roomescape.repository.theme.JdbcThemeRepository;

@JdbcTest
class JdbcReservationWaitingRepositoryTest {

    private JdbcReservationWaitingRepository jdbcReservationWaitingRepository;
    private JdbcReservationRepository jdbcReservationRepository;
    private JdbcReservationSlotRepository jdbcReservationSlotRepository;
    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
        jdbcReservationWaitingRepository = new JdbcReservationWaitingRepository(jdbcTemplate);
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
        jdbcReservationSlotRepository = new JdbcReservationSlotRepository(jdbcTemplate);
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
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

        ReservationWaiting saved = jdbcReservationWaitingRepository.save(waiting);

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

        jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        assertThrows(PersistenceConflictException.class, () -> jdbcReservationWaitingRepository.save(
                ReservationWaiting.createNew(reservation.getSlot(), "아루", LocalDateTime.parse("2026-08-05T12:01:00"))
        ));
    }

    @Test
    @DisplayName("대기 ID로 예약 대기를 조회한다")
    void findById() {
        Reservation reservation = createReservation();
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        ReservationWaiting found = jdbcReservationWaitingRepository.findById(saved.getId())
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
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        jdbcReservationWaitingRepository.delete(saved);

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
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        ReservationWaitingLine waitingLine = jdbcReservationWaitingRepository.findLineBySlot(reservation.getSlot());

        assertThat(waitingLine.isEmpty()).isFalse();
        assertThat(waitingLine.indexOf(saved.getId())).hasValue(0);
    }

    @Test
    @DisplayName("대기 줄에서 첫 번째 대기를 찾는다")
    void findLineBySlotFirst() {
        Reservation reservation = createReservation();
        ReservationWaiting second = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "도기",
                LocalDateTime.parse("2026-08-05T12:01:00")
        ));
        ReservationWaiting first = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
                reservation.getSlot(),
                "아루",
                LocalDateTime.parse("2026-08-05T12:00:00")
        ));

        ReservationWaiting found = jdbcReservationWaitingRepository.findLineBySlot(reservation.getSlot())
                .first()
                .orElseThrow();

        assertThat(found).isEqualTo(first);
        assertThat(found).isNotEqualTo(second);
    }

    private Reservation createReservation() {
        Theme theme = jdbcThemeRepository.save(
                Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
        );
        ReservationTime reservationTime = jdbcReservationTimeRepository.save(
                ReservationTime.createNew(LocalTime.parse("10:00"))
        );

        ReservationSlot slot = jdbcReservationSlotRepository.save(
                new ReservationSlot(LocalDate.parse("2026-08-06"), theme, reservationTime)
        );
        return jdbcReservationRepository.save(Reservation.reserve("쿠다", slot, LocalDateTime.now()));
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
