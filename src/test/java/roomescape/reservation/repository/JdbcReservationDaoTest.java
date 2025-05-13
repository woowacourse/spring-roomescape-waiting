package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.repository.JdbcMemberDao;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeDao;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcReservationTimeDao;

@JdbcTest
@Import({JdbcReservationDao.class, JdbcThemeDao.class, JdbcReservationTimeDao.class, JdbcMemberDao.class})
@Sql({"/test-schema.sql", "/test-member-data.sql", "/test-time-data.sql", "/test-theme-data.sql",
        "/test-reservation-data.sql"})
class JdbcReservationDaoTest {

    @Autowired
    private JdbcReservationDao jdbcReservationDao;

    @Autowired
    private JdbcReservationTimeDao jdbcReservationTimeDao;

    @Autowired
    private JdbcThemeDao jdbcThemeDao;

    @Autowired
    private JdbcMemberDao jdbcMemberDao;

    @DisplayName("새로운 예약을 생성할 수 있다.")
    @Test
    void testSave() {
        // given
        ReservationTime time = jdbcReservationTimeDao.findById(1L).orElseThrow();
        Theme theme = jdbcThemeDao.findById(1L).orElseThrow();
        Member member = jdbcMemberDao.findById(1L).orElseThrow();
        Reservation reservation = Reservation.register(member, LocalDate.now().plusDays(1), time, theme);
        // when
        Reservation savedReservation = jdbcReservationDao.save(reservation);
        // then
        assertAll(
                () -> assertThat(savedReservation.getId()).isEqualTo(4L),
                () -> assertThat(jdbcReservationDao.findAll()).hasSize(4)
        );
    }

    @DisplayName("예약 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // when
        List<Reservation> actual = jdbcReservationDao.findAll();
        // then
        assertThat(actual).hasSize(3);
    }

    @DisplayName("ID로 특정 예약을 삭제할 수 있다.")
    @Test
    void testDeleteById() {
        // when
        jdbcReservationDao.deleteById(2L);
        // then
        List<Reservation> actualThemes = jdbcReservationDao.findAll();
        Condition<Reservation> idIs2L = new Condition<>(reservation -> reservation.getId() == 2L, "Id is 2L");
        assertThat(actualThemes).hasSize(2);
        assertThat(actualThemes).doNotHave(idIs2L);
    }

    @DisplayName("특정 시간을 가지는 예약이 존재하는지 확인할 수 있다.")
    @Test
    void testExistsByTimeId() {
        // given
        long timeId = 1L;
        // when
        boolean actual = jdbcReservationDao.existsByTimeId(timeId);
        // then
        assertThat(actual).isTrue();
    }

    @DisplayName("특정 테마를 가지는 예약이 존재하는지 확인할 수 있다.")
    @Test
    void testExistsByThemeId() {
        // given
        long themeId = 1L;
        // when
        boolean actual = jdbcReservationDao.existsByThemeId(themeId);
        // then
        assertThat(actual).isTrue();
    }

    @DisplayName("날짜, 시간, 테마가 중복되는 예약이 존재하는지 확인할 수 있다.")
    @Test
    void testExistsByDateAndTimeEndTheme() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 1);
        long timeId = 1;
        long themeId = 1;
        // when
        boolean actual = jdbcReservationDao.existsByDateAndTimeAndTheme(date, timeId, themeId);
        // then
        assertThat(actual).isTrue();
    }
}
