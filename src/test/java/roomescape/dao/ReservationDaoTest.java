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
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.Reservation;
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
                .isInstanceOf(DataConflictException.class);
    }

    @Test
    void update_예약_저장_중복_예외() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        ReservationTime ten = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        ReservationTime eleven = new ReservationTime(2L, java.time.LocalTime.of(11, 0));
        Reservation first = reservationDao.save(new Reservation("브라운", date, LocalDateTime.now(), ten, theme));
        reservationDao.save(new Reservation("이든", date, LocalDateTime.now(), eleven, theme));
        Reservation duplicated = new Reservation(first.getId(), first.getName(), date, first.getCreatedAt(), eleven, theme);

        assertThatThrownBy(() -> reservationDao.update(duplicated))
                .isInstanceOf(DataConflictException.class);
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
    void existsByTimeId_대기에만_사용중이면_true() {
        ReservationTime time = new ReservationTime(12L, java.time.LocalTime.of(21, 0));
        Theme theme = new Theme(1L, "공포", "설명", "https://example.com/img.jpg");
        Reservation waiting = new Reservation("이든", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        reservationDao.saveWaiting(waiting);

        assertThat(reservationDao.existsByTimeId(12L)).isTrue();
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
    void existsByThemeId_대기에만_사용중이면_true() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "대기 전용 테마",
                "설명",
                "https://example.com/img.jpg"
        );
        Long themeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
        ReservationTime time = new ReservationTime(12L, java.time.LocalTime.of(21, 0));
        Theme theme = new Theme(themeId, "대기 전용 테마", "설명", "https://example.com/img.jpg");
        Reservation waiting = new Reservation("이든", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        reservationDao.saveWaiting(waiting);

        assertThat(reservationDao.existsByThemeId(themeId)).isTrue();
    }

    @Test
    void existsByThemeId_미사용이면_false() {
        assertThat(reservationDao.existsByThemeId(99L)).isFalse();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_존재하면_true() {
        LocalDate date = LocalDate.now().plusDays(30);
        ReservationTime time = new ReservationTime(3L, java.time.LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        reservationDao.save(new Reservation("브라운", date, LocalDateTime.now(), time, theme));

        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(date, 3L, 1L)).isTrue();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_없으면_false() {
        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(LocalDate.of(2026, 12, 31), 3L, 1L)).isFalse();
    }

    @Test
    void saveWaiting_예약_대기_저장() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);

        Reservation reservationWaiting = reservationDao.saveWaiting(reservation);

        assertThat(reservationWaiting.getId()).isNotNull();
        assertThat(reservationWaiting.getName()).isEqualTo("브리");
    }

    @Test
    void saveWaiting_예약_대기_저장_중복_예외() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);

        reservationDao.saveWaiting(reservation);

        assertThatThrownBy(() -> reservationDao.saveWaiting(reservation))
                .isInstanceOf(DataConflictException.class);
    }

    @Test
    void findByWaitingId_ID로_예약_조회() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        reservationDao.saveWaiting(reservation);
        long reservationId = 1L;

        Reservation actual = reservationDao.findByWaitingId(reservationId).orElse(null);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(reservationId);
    }

    @Test
    void findAllWaitingByName_이름으로_예약_대기__조회() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브리", LocalDate.of(2026, 12, 31), LocalDateTime.now(), time, theme);
        Reservation reservationWaiting = reservationDao.saveWaiting(reservation);

        List<ReservationWaiting> reservationWaitings = reservationDao.findAllWaitingByName(reservationWaiting.getName());
        assertThat(reservationWaitings.size()).isEqualTo(1);
        assertThat(reservationWaitings.getFirst().reservation().getName()).isEqualTo(reservation.getName());
        assertThat(reservationWaitings.getFirst().waitingNumber()).isEqualTo(1);
    }

    @Test
    void findAllWaitingByName_생성_시간이_같으면_ID_순서로_대기_순번_계산() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        LocalDate date = LocalDate.of(2026, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2026, 12, 1, 10, 0);
        Reservation first = new Reservation("브리", date, createdAt, time, theme);
        Reservation second = new Reservation("이든", date, createdAt, time, theme);

        reservationDao.saveWaiting(first);
        reservationDao.saveWaiting(second);

        List<ReservationWaiting> reservationWaitings = reservationDao.findAllWaitingByName("이든");
        assertThat(reservationWaitings).hasSize(1);
        assertThat(reservationWaitings.getFirst().waitingNumber()).isEqualTo(2);
    }

    @Test
    void findFirstWaitingBySlot_가장_먼저_신청한_대기를_조회한다() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        LocalDate date = LocalDate.of(2026, 12, 31);
        Reservation later = new Reservation("나중", date, LocalDateTime.of(2026, 12, 1, 11, 0), time, theme);
        Reservation earlier = new Reservation("먼저", date, LocalDateTime.of(2026, 12, 1, 10, 0), time, theme);

        reservationDao.saveWaiting(later);
        reservationDao.saveWaiting(earlier);

        ReservationWaiting actual = reservationDao.findFirstWaitingBySlot(date, 1L, 1L).orElseThrow();

        assertThat(actual.reservation().getName()).isEqualTo("먼저");
        assertThat(actual.waitingNumber()).isEqualTo(1);
    }

    @Test
    void findFirstWaitingBySlot_생성_시간이_같으면_ID가_작은_대기를_조회한다() {
        ReservationTime time = new ReservationTime(1L, java.time.LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
        LocalDate date = LocalDate.of(2026, 12, 31);
        LocalDateTime createdAt = LocalDateTime.of(2026, 12, 1, 10, 0);
        Reservation first = new Reservation("먼저", date, createdAt, time, theme);
        Reservation second = new Reservation("나중", date, createdAt, time, theme);

        Reservation savedFirst = reservationDao.saveWaiting(first);
        Reservation savedSecond = reservationDao.saveWaiting(second);

        ReservationWaiting actual = reservationDao.findFirstWaitingBySlot(date, 1L, 1L).orElseThrow();

        assertThat(savedFirst.getId()).isLessThan(savedSecond.getId());
        assertThat(actual.reservation().getId()).isEqualTo(savedFirst.getId());
        assertThat(actual.reservation().getName()).isEqualTo("먼저");
        assertThat(actual.waitingNumber()).isEqualTo(1);
    }

    @Test
    void findFirstWaitingBySlot_대기가_없으면_빈_Optional을_반환한다() {
        assertThat(reservationDao.findFirstWaitingBySlot(LocalDate.of(2026, 12, 31), 1L, 1L)).isEmpty();
    }
}
