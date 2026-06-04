package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

class ReservationDaoTest {

    private EmbeddedDatabase dataSource;
    private ReservationDao reservationDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        reservationDao = new ReservationDao(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void findAll_전체_예약_조회() {
        List<Reservation> reservations = reservationDao.findAll(0, 100);

        assertThat(reservations).isNotEmpty();
        assertThat(reservations).allMatch(r -> r.getName() != null && r.getDate() != null);
    }

    @Test
    void findByName_이름으로_예약_조회() {
        List<Reservation> reservations = reservationDao.findByName("김철수");

        assertThat(reservations).hasSize(4);
        assertThat(reservations).allMatch(r -> r.getName().equals("김철수"));
    }

    @Test
    void findByName_없는_이름이면_빈_목록() {
        List<Reservation> reservations = reservationDao.findByName("없는사람");

        assertThat(reservations).isEmpty();
    }

    @Test
    void save_예약_저장() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);

        Reservation saved = reservationDao.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("브라운");
    }

    @Test
    void save_예약_저장_중복_예외() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);

        reservationDao.save(reservation);
        assertThatThrownBy(() -> reservationDao.save(reservation))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void delete_예약_삭제() {
        int beforeSize = reservationDao.findAll(0, 100).size();
        reservationDao.delete(1L);

        assertThat(reservationDao.findAll(0, 100)).hasSize(beforeSize - 1);
    }

    @Test
    void existsByTimeId_사용중이면_true() {
        assertThat(reservationDao.existsByTimeId(3L)).isTrue();
    }

    @Test
    void existsByTimeId_미사용이면_false() {
        assertThat(reservationDao.existsByTimeId(12L)).isFalse();
    }

    @Test
    void existsByThemeId_사용중이면_true() {
        assertThat(reservationDao.existsByThemeId(1L)).isTrue();
    }

    @Test
    void existsByThemeId_미사용이면_false() {
        assertThat(reservationDao.existsByThemeId(99L)).isFalse();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_존재하면_true() {
        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(LocalDate.of(2026, 4, 29), 3L, 1L)).isTrue();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_없으면_false() {
        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(LocalDate.of(2026, 12, 31), 3L, 1L)).isFalse();
    }

    @Test
    void findWaitingById_대기_단건_조회() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation saved = reservationDao.save(
                new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme, ReservationStatus.WAITING));

        Reservation found = reservationDao.findWaitingById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("브리");
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void findWaitingById_CONFIRMED_예약은_조회_안됨() {
        assertThat(reservationDao.findWaitingById(1L)).isEmpty();
    }

    @Test
    void findAllWaitingByName_이름으로_대기_목록과_순서_조회() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        LocalDate date = LocalDate.of(2026, 12, 31);
        reservationDao.save(new Reservation("브리", date, LocalDateTime.of(2026, 12, 1, 10, 0), time, theme, ReservationStatus.WAITING));
        reservationDao.save(new Reservation("이영희", date, LocalDateTime.of(2026, 12, 1, 11, 0), time, theme, ReservationStatus.WAITING));

        List<ReservationWaiting> waitings = reservationDao.findAllWaitingByName("브리");

        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst().reservation().getName()).isEqualTo("브리");
        assertThat(waitings.getFirst().waitingNumber()).isEqualTo(1);
    }

    @Test
    void findAllWaitingByName_동시_대기_신청시_id_기준으로_순서_보장() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        LocalDate date = LocalDate.of(2026, 12, 31);
        LocalDateTime sameTime = LocalDateTime.of(2026, 12, 1, 10, 0);

        reservationDao.save(new Reservation("브리", date, sameTime, time, theme, ReservationStatus.WAITING));
        reservationDao.save(new Reservation("이영희", date, sameTime, time, theme, ReservationStatus.WAITING));

        List<ReservationWaiting> waitings = reservationDao.findAllWaitingByName("브리");

        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst().waitingNumber()).isEqualTo(1L);
    }

    @Test
    void existsWaitingByDateAndTimeIdAndThemeIdAndName_대기_중복_확인() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        reservationDao.save(new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme, ReservationStatus.WAITING));

        assertThat(reservationDao.existsWaitingByDateAndTimeIdAndThemeIdAndName(
                LocalDate.of(2026, 12, 31), 1L, 1L, "브리")).isTrue();
        assertThat(reservationDao.existsWaitingByDateAndTimeIdAndThemeIdAndName(
                LocalDate.of(2026, 12, 31), 1L, 1L, "없는사람")).isFalse();
    }

    @Test
    void findFirstWaitingByDateAndTimeIdAndThemeId_첫번째_대기자_반환() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        LocalDate date = LocalDate.of(2026, 12, 31);

        Reservation first = reservationDao.save(new Reservation("브리", date, LocalDateTime.of(2026, 12, 1, 9, 0), time, theme, ReservationStatus.WAITING));
        reservationDao.save(new Reservation("이영희", date, LocalDateTime.of(2026, 12, 1, 10, 0), time, theme, ReservationStatus.WAITING));

        Reservation found = reservationDao.findFirstWaitingByDateAndTimeIdAndThemeId(date, 1L, 1L).orElseThrow();

        assertThat(found.getId()).isEqualTo(first.getId());
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void findFirstWaitingByDateAndTimeIdAndThemeId_대기자_없으면_empty() {
        assertThat(reservationDao.findFirstWaitingByDateAndTimeIdAndThemeId(
                LocalDate.of(2026, 12, 31), 1L, 1L)).isEmpty();
    }

    @Test
    void updateStatus_WAITING에서_CONFIRMED로_전환() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation waiting = reservationDao.save(
                new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme, ReservationStatus.WAITING));

        reservationDao.updateStatus(waiting.getId(), ReservationStatus.CONFIRMED);

        Reservation updated = reservationDao.findById(waiting.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

}
