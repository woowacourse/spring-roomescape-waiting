package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.member.domain.MemberRole.ADMIN;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.domain.Member;
import roomescape.member.repository.JdbcMemberDao;
import roomescape.member.repository.MemberDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@JdbcTest
@Sql(scripts = "/schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationDaoTest {

    private ReservationDao reservationDao;
    private ReservationTimeDao reservationTimeDao;
    private ThemeDao themeDao;
    private MemberDao memberDao;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;
    private final LocalDate date = LocalDate.of(2025, 9, 24);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        reservationDao = new JdbcReservationDao(namedParameterJdbcTemplate);
        reservationTimeDao = new JdbcReservationTimeDao(namedParameterJdbcTemplate);
        themeDao = new JdbcThemeDao(namedParameterJdbcTemplate);
        memberDao = new JdbcMemberDao(namedParameterJdbcTemplate);
        reservationTime = reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeDao.save(new Theme(null, "우테코방탈출", "탈출탈출탈출", "abcdefg"));
        member = memberDao.save(new Member(null, "레오", "admin@gmail.com", "qwer!", ADMIN));
    }

    @DisplayName("새로운 예약을 저장할 수 있다")
    @Test
    void save() {
        // given
        Reservation newReservation = new Reservation(null, member, date, reservationTime, theme);
        // when
        Reservation result = reservationDao.save(newReservation);
        // then
        Reservation savedReservation = reservationDao.findAll().getFirst();
        assertAll(
                () -> assertThat(result.getName()).isEqualTo("레오"),
                () -> assertThat(result.getDate()).isEqualTo(date),
                () -> assertThat(result.getTime()).isEqualTo(reservationTime),
                () -> assertThat(result.getTheme()).isEqualTo(theme),
                () -> assertThat(savedReservation.getName()).isEqualTo("레오"),
                () -> assertThat(savedReservation.getDate()).isEqualTo(date),
                () -> assertThat(savedReservation.getTime()).isEqualTo(reservationTime),
                () -> assertThat(savedReservation.getTheme()).isEqualTo(theme)
        );
    }

    @DisplayName("예약을 삭제할 수 있다")
    @Test
    void deleteById() {
        // given
        Reservation savedReservation = reservationDao.save(new Reservation(null, member, date, reservationTime, theme));
        // when
        reservationDao.deleteById(savedReservation.getId());
        // then
        assertThat(reservationDao.findAll()).isEmpty();
    }

    @DisplayName("예약 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        reservationDao.save(new Reservation(null, member, date, reservationTime, theme));
        reservationDao.save(new Reservation(null, member, date.plusDays(1), reservationTime, theme));
        // when
        List<Reservation> result = reservationDao.findAll();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약 날짜와 시간을 기반으로 예약이 존재하는 지 여부를 반환할 수 있다")
    @Test
    void isExistsByDateAndTimeIdAndThemeId() {
        // given
        reservationDao.save(new Reservation(null, member, date, reservationTime, theme));
        // when
        boolean result = reservationDao.isExistsByDateAndTimeIdAndThemeId(date, reservationTime.getId(), theme.getId());
        // then
        assertThat(result).isTrue();
    }

    @DisplayName("시간을 기반으로 예약이 존재하는 지 여부를 반환할 수 있다")
    @Test
    void isExistsByTimeId() {
        // given
        reservationDao.save(new Reservation(null, member, date, reservationTime, theme));
        // when
        boolean result = reservationDao.isExistsByTimeId(reservationTime.getId());
        // then
        assertThat(result).isTrue();
    }

    @DisplayName("테마를 기반으로 예약이 존재하는 지 여부를 반환할 수 있다")
    @Test
    void isExistsByThemeId() {
        // given
        reservationDao.save(new Reservation(null, member, date, reservationTime, theme));
        // when
        boolean result = reservationDao.isExistsByThemeId(theme.getId());
        // then
        assertThat(result).isTrue();
    }

    @DisplayName("날짜와 테마를 기반으로 예약 목록을 조회할 수 있다")
    @Test
    void findAllByDateAndThemeId() {
        // given
        String name = "leo";
        reservationDao.save(new Reservation(null, member, date, reservationTime, theme));
        reservationDao.save(new Reservation(null, member, date.plusDays(1), reservationTime, theme));
        // when
        List<Reservation> result = reservationDao.findAllByDateAndThemeId(date, theme.getId());
        // then
        assertThat(result).hasSize(1);
    }
}
