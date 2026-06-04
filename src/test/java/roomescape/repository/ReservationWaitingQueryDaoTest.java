package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
public class ReservationWaitingQueryDaoTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationWaitingQueryDao reservationWaitingQueryingDao;

    @BeforeEach
    void setUp() {
        this.reservationWaitingQueryingDao = new ReservationWaitingQueryDao(jdbcTemplate);

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
        jdbcTemplate.update("insert into waiting (name, date, time_id, theme_id, created_at) values ('테스트', '2027-05-27', 1, 1, '2026-05-15 10:30:00')");
    }

    @Test
    void 예약_대기가_제대로_존재하는_지_조회한다() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "테스트", "설명", "url");

        assertThat(reservationWaitingQueryingDao.isExistByNameAndSlot("테스트", new ReservationSlot(LocalDate.parse("2027-05-27"), reservationTime, theme))).isTrue();
        assertThat(reservationWaitingQueryingDao.isExistByNameAndSlot("테스트", new ReservationSlot(LocalDate.parse("2027-05-26"), reservationTime, theme))).isFalse();
    }

    @Test
    void 예약_대기가_id로_정상_조회한다() {
        assertThat(reservationWaitingQueryingDao.findReservationWaitingById(1).isPresent()).isTrue();
        assertThat(reservationWaitingQueryingDao.findReservationWaitingById(1).get().getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_대기_전체_조회가_정상_조회한다() {
        assertThat(reservationWaitingQueryingDao.findAllReservationWaiting().size()).isEqualTo(1);
    }

    @Test
    void 예약_대기가_이름으로_정상_조회한다() {
        assertThat(reservationWaitingQueryingDao.findAllByName("테스트").size()).isEqualTo(1);
        assertThat(reservationWaitingQueryingDao.findAllByName("테스트").getFirst().getCreatedAt())
                .isEqualTo(LocalDateTime.parse("2026-05-15T10:30:00"));
    }

    @Test
    void 같은_슬롯에_대기_여러개면_가장_먼저_등록된_순번_반환한다() {
        jdbcTemplate.update("insert into waiting (name, date, time_id, theme_id, created_at) values ('네오', '2027-05-27', 1, 1, '2026-05-15 11:00:00')");

        ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "테스트", "설명", "url");
        ReservationSlot slot = new ReservationSlot(LocalDate.parse("2027-05-27"), time, theme);

        assertThat(reservationWaitingQueryingDao.findFirstWaitingBySlot(slot))
                .isPresent()
                .get()
                .extracting(ReservationWaiting::getName)
                .isEqualTo("테스트");
    }

    @Test
    void 대기_없는_슬롯이면_비어있는_Optional을_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "테스트", "설명", "url");
        ReservationSlot slot = new ReservationSlot(LocalDate.parse("2030-05-27"), time, theme);

        assertThat(reservationWaitingQueryingDao.findFirstWaitingBySlot(slot)).isEmpty();
    }
}
