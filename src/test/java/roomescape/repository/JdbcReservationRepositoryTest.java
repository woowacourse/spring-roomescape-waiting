package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
public class JdbcReservationRepositoryTest {

    private ReservationRepository reservationRepository;

    private ReservationTime reservationTime;

    private Theme theme;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);

        String insertReservationTimeSql = "INSERT INTO `reservation_time` (`start_at`) VALUES (?)";
        jdbcTemplate.update(insertReservationTimeSql, "10:00");
        jdbcTemplate.update(insertReservationTimeSql, "11:00");
        jdbcTemplate.update(insertReservationTimeSql, "12:00");

        String insertThemeSql = "INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertThemeSql, "방탈출1", "방탈출1 설명", "url.jpg");

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "방탈출1", "방탈출1 설명", "url.jpg");
    }

    @AfterEach
    void afterEach() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1");
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void saveTest() {
        Reservation reservationWithoutId = new Reservation("fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);

        Reservation reservation = reservationRepository.save(reservationWithoutId);

        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        Reservation reservation = reservationRepository.findById(1L).get();

        assertThat(reservation.getName()).isEqualTo("fizz");
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(reservation.getTime().getId()).isEqualTo(1L);
        assertThat(reservation.getTheme().getId()).isEqualTo(1L);
    }

    @Test
    void findByNameTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(sql, "tree", "2026-05-02", 2L, 1L);
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 3L, 1L);

        List<Reservation> reservations = reservationRepository.findByName("fizz");

        assertThat(reservations.size()).isEqualTo(2);
        assertThat(reservations.get(0).getName()).isEqualTo("fizz");
        assertThat(reservations.get(1).getName()).isEqualTo("fizz");

        assertThat(reservationRepository.findByName("user").size()).isEqualTo(0);
    }

    @Test
    void findByNameDoesNotReturnPendingReservationTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);

        List<Reservation> reservations = reservationRepository.findByName("fizz");

        assertThat(reservations).isEmpty();
    }

    @Test
    void findAllTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 2L, 1L);

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void findAllDoesNotReturnPendingReservationTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations).isEmpty();
    }

    @Test
    void savePendingReservationAndFindByOrderIdTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);

        Reservation saved = reservationRepository.save(pendingReservation);
        Reservation found = reservationRepository.findByOrderId("order_test").get();

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(found.getOrderId()).isEqualTo("order_test");
        assertThat(found.getIdempotencyKey()).isEqualTo("order_test");
        assertThat(found.getAmount()).isEqualTo(50000L);
    }

    @Test
    void findPaymentHistoryByNameReturnsPendingAndConfirmedReservationsTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);
        reservationRepository.startPaymentConfirmation("order_test");
        reservationRepository.confirmPayment("order_test", "payment_key");

        ReservationTime otherReservationTime = new ReservationTime(2L, LocalTime.of(11, 0));
        Reservation otherPendingReservation = Reservation.pending("buzz", LocalDate.of(2026, 5, 2),
                otherReservationTime, theme, "order_other", 50000L);
        reservationRepository.save(otherPendingReservation);

        List<Reservation> paymentHistory = reservationRepository.findPaymentHistoryByName("fizz");

        assertThat(paymentHistory).hasSize(1);
        assertThat(paymentHistory.get(0).getOrderId()).isEqualTo("order_test");
        assertThat(paymentHistory.get(0).getPaymentKey()).isEqualTo("payment_key");
    }

    @Test
    void confirmPaymentTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);
        reservationRepository.startPaymentConfirmation("order_test");

        Reservation confirmed = reservationRepository.confirmPayment("order_test", "payment_key");

        assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(confirmed.getPaymentKey()).isEqualTo("payment_key");
        assertThat(reservationRepository.findAll()).hasSize(1);
        assertThat(reservationRepository.findByName("fizz")).hasSize(1);
    }

    @Test
    void markPaymentUnknownTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);
        reservationRepository.startPaymentConfirmation("order_test");

        Reservation unknown = reservationRepository.markPaymentUnknown("order_test");

        assertThat(unknown.getStatus()).isEqualTo(ReservationStatus.PAYMENT_UNKNOWN);
        assertThat(unknown.getOrderId()).isEqualTo("order_test");
        assertThat(unknown.getAmount()).isEqualTo(50000L);
        assertThat(reservationRepository.findByOrderId("order_test").get().getStatus())
                .isEqualTo(ReservationStatus.PAYMENT_UNKNOWN);
    }

    @Test
    void startPaymentConfirmationTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);

        Reservation confirming = reservationRepository.startPaymentConfirmation("order_test");

        assertThat(confirming.getStatus()).isEqualTo(ReservationStatus.PAYMENT_CONFIRMING);
        assertThat(reservationRepository.findByOrderId("order_test").get().getStatus())
                .isEqualTo(ReservationStatus.PAYMENT_CONFIRMING);
    }

    @Test
    void releasePaymentConfirmationTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);
        reservationRepository.startPaymentConfirmation("order_test");

        Reservation pending = reservationRepository.releasePaymentConfirmation("order_test");

        assertThat(pending.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservationRepository.findByOrderId("order_test").get().getStatus())
                .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void deletePendingByOrderIdTest() {
        Reservation pendingReservation = Reservation.pending("fizz", LocalDate.of(2026, 5, 2), reservationTime,
                theme, "order_test", 50000L);
        reservationRepository.save(pendingReservation);

        reservationRepository.deletePendingByOrderId("order_test");

        assertThat(reservationRepository.findByOrderId("order_test")).isEmpty();
    }

    @Test
    void deleteStalePendingBeforeTest() {
        String sql = """
                INSERT INTO reservation(name, date, time_id, theme_id, status, order_id, idempotency_key, amount,
                                        created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, "old", "2026-05-02", 1L, 1L, ReservationStatus.PENDING.name(),
                "order_old", "order_old", 50000L, LocalDateTime.of(2026, 5, 2, 8, 49));
        jdbcTemplate.update(sql, "fresh", "2026-05-02", 2L, 1L, ReservationStatus.PENDING.name(),
                "order_fresh", "order_fresh", 50000L, LocalDateTime.of(2026, 5, 2, 8, 50));

        reservationRepository.deleteStalePendingBefore(LocalDateTime.of(2026, 5, 2, 8, 50));

        assertThat(reservationRepository.findByOrderId("order_old")).isEmpty();
        assertThat(reservationRepository.findByOrderId("order_fresh")).isPresent();
    }

    @Test
    void deleteTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        reservationRepository.delete(1L);

        String findReservationCountSql = "SELECT COUNT(*) FROM `reservation`";
        int count = jdbcTemplate.queryForObject(findReservationCountSql, Integer.class);

        Assertions.assertThat(count).isEqualTo(0);
    }

    @Test
    void existsByTimeIdTest() {
        boolean exist = reservationRepository.existsByTimeId(1L);
        assertThat(exist).isFalse();

        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        exist = reservationRepository.existsByTimeId(1L);
        assertThat(exist).isTrue();
    }

    @Test
    void existsByThemeIdTest() {
        jdbcTemplate.update("INSERT INTO `theme` (`name`, `description`, `thumbnail_url`) VALUES (?, ?, ?)",
                "방탈출2", "방탈출2 설명", "url2.jpg");

        // time_id=1, theme_id=2 로 의도적으로 다르게 설정
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 2L);

        assertThat(reservationRepository.existsByThemeId(2L)).isTrue();
        assertThat(reservationRepository.existsByThemeId(1L)).isFalse();
    }

    @Test
    void findBySlotTest() {
        String sql = "INSERT INTO `reservation` (`name`, `date`, `time_id`, `theme_id`) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, "fizz", "2026-05-02", 1L, 1L);

        Optional<Reservation> slot = reservationRepository.findBySlot(LocalDate.of(2026, 5, 2), 1L, 1L);

        assertThat(slot).isNotEmpty();
        assertThat(slot.get().getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(slot.get().getName()).isEqualTo("fizz");
        assertThat(slot.get().getTime().getId()).isEqualTo(1L);
        assertThat(slot.get().getTheme().getId()).isEqualTo(1L);
    }
}
