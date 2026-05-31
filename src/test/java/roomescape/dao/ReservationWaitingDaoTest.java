package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

class ReservationWaitingDaoTest {

    private EmbeddedDatabase dataSource;
    private ReservationDao reservationDao;
    private ReservationWaitingDao reservationWaitingDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        reservationDao = new ReservationDao(jdbcTemplate);
        reservationWaitingDao = new ReservationWaitingDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    private Reservation savedReservation(String name, LocalDate date) {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(), time, theme);
        return reservationDao.save(reservation);
    }

    @Test
    void saveWaiting_대기_저장() {
        Reservation saved = savedReservation("브리", LocalDate.of(2026, 12, 31));

        ReservationWaiting waiting = reservationWaitingDao.saveWaiting(saved);

        assertThat(waiting.id()).isGreaterThan(0);
        assertThat(waiting.id()).isNotEqualTo(saved.getId());
        assertThat(waiting.reservation().getName()).isEqualTo("브리");
    }

    @Test
    void saveWaiting_중복_시_DuplicateKeyException() {
        Reservation saved = savedReservation("브리", LocalDate.of(2026, 12, 31));
        reservationWaitingDao.saveWaiting(saved);

        Reservation saved2 = savedReservation("브리2", LocalDate.of(2026, 12, 31));
        reservationWaitingDao.saveWaiting(saved2);

        assertThatThrownBy(() -> reservationWaitingDao.saveWaiting(saved))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void findByWaitingId_ID로_대기_조회() {
        Reservation saved = savedReservation("브리", LocalDate.of(2026, 12, 31));
        ReservationWaiting savedWaiting = reservationWaitingDao.saveWaiting(saved);

        ReservationWaiting actual = reservationWaitingDao.findByWaitingId(savedWaiting.id()).orElse(null);

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(savedWaiting.id());
        assertThat(actual.reservation().getName()).isEqualTo("브리");
    }

    @Test
    void deleteWaiting_대기_삭제() {
        Reservation saved = savedReservation("브리", LocalDate.of(2026, 12, 31));
        ReservationWaiting savedWaiting = reservationWaitingDao.saveWaiting(saved);

        reservationWaitingDao.deleteWaiting(savedWaiting.id());

        assertThat(reservationWaitingDao.findByWaitingId(savedWaiting.id())).isEmpty();
    }

    @Test
    void findAllWaitingByName_이름으로_대기_목록_조회() {
        Reservation saved = savedReservation("브리", LocalDate.of(2026, 12, 31));
        reservationWaitingDao.saveWaiting(saved);

        List<ReservationWaiting> waitings = reservationWaitingDao.findAllWaitingByName("브리");

        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst().reservation().getName()).isEqualTo("브리");
        assertThat(waitings.getFirst().waitingNumber()).isEqualTo(1);
    }

    @Test
    void existsByDateAndTimeIdAndThemeIdAndName_대기_중복_확인() {
        Reservation saved = savedReservation("브리", LocalDate.of(2026, 12, 31));
        reservationWaitingDao.saveWaiting(saved);

        assertThat(reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(
                LocalDate.of(2026, 12, 31), 1L, 1L, "브리")).isTrue();
        assertThat(reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(
                LocalDate.of(2026, 12, 31), 1L, 1L, "없는사람")).isFalse();
    }
}
