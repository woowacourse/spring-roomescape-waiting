//package roomescape.reservationwaiting.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import roomescape.reservation.Reservation;
//import roomescape.reservation.repository.JdbcReservationRepository;
//import roomescape.reservationtime.ReservationTime;
//import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
//import roomescape.reservationwaiting.ReservationWaiting;
//import roomescape.theme.Theme;
//import roomescape.theme.repository.JdbcThemeRepository;
//
//@JdbcTest
//class JdbcReservationWaitingRepositoryTest {
//
//    private JdbcReservationWaitingRepository jdbcReservationWaitingRepository;
//    private JdbcReservationRepository jdbcReservationRepository;
//    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
//    private JdbcThemeRepository jdbcThemeRepository;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @BeforeEach
//    void setup() {
//        clearTables();
//        jdbcReservationWaitingRepository = new JdbcReservationWaitingRepository(jdbcTemplate);
//        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
//        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
//        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
//    }
//
//    @Test
//    @DisplayName("예약 대기를 저장한다")
//    void save() {
//        Reservation reservation = createReservation();
//        ReservationWaiting waiting = ReservationWaiting.createNew(
//                LocalDate.now().plusDays(1),
//                1L,
//                1L,
//                "아루",
//                LocalDateTime.now().minusDays(1)
//        );
//
//        ReservationWaiting saved = jdbcReservationWaitingRepository.save(waiting);
//
//        assertThat(saved.getId()).isEqualTo(1L);
//
//        String name = jdbcTemplate.queryForObject(
//                "SELECT name FROM reservation_waiting WHERE id = ?",
//                String.class,
//                saved.getId()
//        );
//
//        assertThat(name).isEqualTo("아루");
//    }
//
//    @Test
//    @DisplayName("같은 예약에 같은 이름으로 중복 대기를 저장할 수 없다")
//    void saveDuplicateNameForReservation() {
//        Reservation reservation = createReservation();
//
//        jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
//                LocalDate.now().plusDays(1),
//                1L,
//                1L,
//                "아루",
//                LocalDateTime.now().minusDays(1)
//        ));
//
//        assertThrows(DataIntegrityViolationException.class, () -> jdbcReservationWaitingRepository.save(
//                ReservationWaiting.createNew(LocalDate.now().plusDays(1), 1L, 1L, "아루", LocalDateTime.now().minusDays(1))
//        ));
//    }
//
//    @Test
//    @DisplayName("대기 ID와 이름으로 예약 대기를 삭제한다")
//    void deleteByIdAndName() {
//        Reservation reservation = createReservation();
//        ReservationWaiting saved = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
//                LocalDate.now().plusDays(1),
//                1L,
//                1L,
//                "아루",
//                LocalDateTime.now().minusDays(1)
//        ));
//
//        int affectedRowCount = jdbcReservationWaitingRepository.deleteByIdAndName(saved.getId(), "아루");
//
//        Integer count = jdbcTemplate.queryForObject(
//                "SELECT count(1) FROM reservation_waiting WHERE id = ?",
//                Integer.class,
//                saved.getId()
//        );
//
//        assertThat(affectedRowCount).isOne();
//        assertThat(count).isZero();
//    }
//
//    @Test
//    @DisplayName("대기 ID가 같아도 이름이 다르면 예약 대기를 삭제하지 않는다")
//    void deleteByIdAndDifferentName() {
//        Reservation reservation = createReservation();
//        ReservationWaiting saved = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
//                LocalDate.now().plusDays(1),
//                1L,
//                1L,
//                "아루",
//                LocalDateTime.now().minusDays(1)
//        ));
//
//        int affectedRowCount = jdbcReservationWaitingRepository.deleteByIdAndName(saved.getId(), "다른이름");
//
//        Integer count = jdbcTemplate.queryForObject(
//                "SELECT count(1) FROM reservation_waiting WHERE id = ?",
//                Integer.class,
//                saved.getId()
//        );
//
//        assertThat(affectedRowCount).isZero();
//        assertThat(count).isOne();
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 대기 ID는 삭제 건수가 0이다")
//    void deleteByNotFoundId() {
//        Reservation reservation = createReservation();
//        ReservationWaiting saved = jdbcReservationWaitingRepository.save(ReservationWaiting.createNew(
//                reservation.getDate(),
//                reservation.getTheme().getId(),
//                reservation.getTime().getId(),
//                "아루",
//                LocalDateTime.now()
//        ));
//
//        int affectedRowCount = jdbcReservationWaitingRepository.deleteByIdAndName(999L, "아루");
//
//        Integer count = jdbcTemplate.queryForObject(
//                "SELECT count(1) FROM reservation_waiting WHERE id = ?",
//                Integer.class,
//                saved.getId()
//        );
//
//        assertThat(affectedRowCount).isZero();
//        assertThat(count).isOne();
//    }
//
//    private Reservation createReservation() {
//        Theme theme = jdbcThemeRepository.save(
//                Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
//        );
//        ReservationTime reservationTime = jdbcReservationTimeRepository.save(
//                ReservationTime.createNew(LocalTime.parse("10:00"))
//        );
//
//        return jdbcReservationRepository.save(
//                Reservation.createNew("쿠다", LocalDate.parse("2026-08-06"), theme, reservationTime)
//        );
//    }
//
//    private void clearTables() {
//        jdbcTemplate.update("DELETE FROM reservation_waiting");
//        jdbcTemplate.update("DELETE FROM reservation");
//        jdbcTemplate.update("DELETE FROM reservation_time");
//        jdbcTemplate.update("DELETE FROM theme");
//        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
//        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
//        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
//        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
//    }
//}
