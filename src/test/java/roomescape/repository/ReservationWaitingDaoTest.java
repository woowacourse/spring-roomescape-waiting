package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
public class ReservationWaitingDaoTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "테스트", "설명", "url");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationWaitingDao reservationWaitingDao;

    @BeforeEach
    void setUp() {
        this.reservationWaitingDao = new ReservationWaitingDao(jdbcTemplate);

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
        jdbcTemplate.update("insert into reservation (name, date, time_id, theme_id, created_at) values ('예약자', '2027-05-27', 1, 1, '2026-05-01 09:00:00')");
        jdbcTemplate.update("insert into waiting (name, reservation_id, created_at) values ('테스트', 1, '2026-05-15 10:30:00')");
    }

    @Test
    void 예약_대기가_제대로_존재하는_지_조회한다() {
        assertThat(reservationWaitingDao.isExistByNameAndReservationId("테스트", 1L)).isTrue();
        assertThat(reservationWaitingDao.isExistByNameAndReservationId("없는사람", 1L)).isFalse();
    }

    @Test
    void 예약_대기가_id로_정상_조회한다() {
        assertThat(reservationWaitingDao.findReservationWaitingById(1).isPresent()).isTrue();
        assertThat(reservationWaitingDao.findReservationWaitingById(1).get().getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_대기_전체_조회가_정상_조회한다() {
        assertThat(reservationWaitingDao.findAllReservationWaiting().size()).isEqualTo(1);
    }

    @Test
    void 예약_대기가_이름으로_정상_조회한다() {
        assertThat(reservationWaitingDao.findAllByName("테스트").size()).isEqualTo(1);
        assertThat(reservationWaitingDao.findAllByName("테스트").get(0).getCreatedAt())
                .isEqualTo(LocalDateTime.parse("2026-05-15T10:30:00"));
    }

    @Test
    void 예약_대기를_제대로_생성한다() {
        Reservation reservation = Reservation.restore(1L, "예약자", LocalDate.parse("2027-05-27"), reservationTime, theme, LocalDateTime.now());
        ReservationWaiting reservationWaiting = ReservationWaiting.create("새사람", reservation);

        reservationWaitingDao.create(reservationWaiting);

        Optional<ReservationWaiting> found = reservationWaitingDao.findAllByName("새사람").stream().findFirst();
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getReservation().getDate()).isEqualTo(LocalDate.parse("2027-05-27"));
    }

    @Test
    void 예약_대기를_제대로_삭제한다() {
        String sql = "SELECT EXISTS (SELECT 1 FROM waiting WHERE id = ?)";

        assertThat(jdbcTemplate.queryForObject(sql, Boolean.class, 1L)).isTrue();

        reservationWaitingDao.delete(1L);

        assertThat(jdbcTemplate.queryForObject(sql, Boolean.class, 1L)).isFalse();
    }
}
