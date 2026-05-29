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
    private ReservationWaitingDao reservationWaitingDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        reservationWaitingDao = new ReservationWaitingDao(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void saveWaiting_대기_저장() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);

        ReservationWaiting waiting = reservationWaitingDao.saveWaiting(reservation);

        assertThat(waiting.id()).isGreaterThan(0);
        assertThat(waiting.reservation().getName()).isEqualTo("브리");
    }

    @Test
    void saveWaiting_중복_시_DuplicateKeyException() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);

        reservationWaitingDao.saveWaiting(reservation);

        assertThatThrownBy(() -> reservationWaitingDao.saveWaiting(reservation))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void findByWaitingId_ID로_대기_조회() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        ReservationWaiting saved = reservationWaitingDao.saveWaiting(reservation);

        ReservationWaiting actual = reservationWaitingDao.findByWaitingId(saved.id()).orElse(null);

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(saved.id());
        assertThat(actual.reservation().getName()).isEqualTo("브리");
    }

    @Test
    void deleteWaiting_대기_삭제() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        ReservationWaiting saved = reservationWaitingDao.saveWaiting(reservation);

        reservationWaitingDao.deleteWaiting(saved.id());

        assertThat(reservationWaitingDao.findByWaitingId(saved.id())).isEmpty();
    }

    @Test
    void findAllWaitingByName_이름으로_대기_목록_조회() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        reservationWaitingDao.saveWaiting(reservation);

        List<ReservationWaiting> waitings = reservationWaitingDao.findAllWaitingByName("브리");

        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst().reservation().getName()).isEqualTo("브리");
        assertThat(waitings.getFirst().waitingNumber()).isEqualTo(1);
    }

    @Test
    void existsByDateAndTimeIdAndThemeIdAndName_대기_중복_확인() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        reservationWaitingDao.saveWaiting(reservation);

        assertThat(reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(
                LocalDate.of(2026, 12, 31), 1L, 1L, "브리")).isTrue();
        assertThat(reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(
                LocalDate.of(2026, 12, 31), 1L, 1L, "없는사람")).isFalse();
    }
}
