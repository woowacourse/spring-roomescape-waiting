package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.utils.JdbcTemplateUtils;

@ActiveProfiles("test")
@JdbcTest
@Import({ReservationDao.class})
class ReservationDaoTest {

    private static final Long RESERVATION_TIME_ID = 1L;
    private static final LocalTime RESERVATION_TIME_START_TIME = LocalTime.of(8, 0);
    private static final Long THEME_ID = 1L;
    private static final String THEME_NAME = "공포";
    private static final String THEME_DESCRIPTION = "우테코 공포";
    private static final String THEME_THUMBNAIL = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";
    private static final Long MEMBER_ID = 2L;
    private static final String NAME = "포스티";
    private static final String EMAIL = "posty@woowa.com";
    private static final String PASSWORD = "12341234";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReservationDao reservationDao;

    @AfterEach
    void tearDown() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
    }

    @DisplayName("예약 정보를 저장한다.")
    @Test
    void test1() {
        // given
        ReservationTime reservationTime = saveReservationTime(RESERVATION_TIME_ID, RESERVATION_TIME_START_TIME);
        Theme theme = saveTheme(THEME_ID, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL);
        Member member = saveMember(MEMBER_ID, NAME, EMAIL, PASSWORD);

        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = Reservation.withoutId(member, now.toLocalDate(), reservationTime, theme);

        // when
        Reservation result = reservationDao.save(reservation);

        // then
        assertThat(result.getMember().getName()).isEqualTo(NAME);
        assertThat(result.getReservationDate()).isEqualTo(now.toLocalDate());
        assertThat(result.getReservationTime().getId()).isEqualTo(RESERVATION_TIME_ID);
        assertThat(result.getTheme().getId()).isEqualTo(THEME_ID);
    }

    @DisplayName("id로 예약 정보를 가져온다")
    @Test
    void test5() {
        // given
        LocalDateTime now = LocalDateTime.now();

        saveReservationTime(RESERVATION_TIME_ID, now.toLocalTime());
        saveTheme(THEME_ID, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL);
        saveMember(MEMBER_ID, NAME, EMAIL, PASSWORD);

        long id = 1;
        saveReservation(id, now.toLocalDate(), RESERVATION_TIME_ID, THEME_ID, MEMBER_ID);

        // when
        Reservation result = reservationDao.findById(id).get();

        // then
        assertThat(result.getReservationDate()).isEqualTo(now.toLocalDate());
        assertThat(result.getReservationTimeId()).isEqualTo(RESERVATION_TIME_ID);
        assertThat(result.getReservationStartTime()).isEqualTo(now.toLocalTime());
        assertThat(result.getThemeId()).isEqualTo(THEME_ID);
        assertThat(result.getMember().getId()).isEqualTo(MEMBER_ID);
    }

    @DisplayName("모든 예약 정보를 가져온다.")
    @Test
    void test6() {
        // given
        LocalDate date = LocalDate.now();
        saveReservationTime(RESERVATION_TIME_ID, RESERVATION_TIME_START_TIME);
        saveTheme(THEME_ID, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL);
        saveMember(MEMBER_ID, NAME, EMAIL, PASSWORD);

        String sql = "insert into reservation (date, time_id, theme_id, member_id) values (?, ?, ?, ?)";
        jdbcTemplate.update(sql, date, RESERVATION_TIME_ID, THEME_ID, MEMBER_ID);

        // when
        List<Reservation> result = reservationDao.findAll();

        // then
        assertThat(result).hasSize(1);
    }

    @DisplayName("예약 정보를 삭제한다.")
    @Test
    void test7() {
        // given
        Long reservationId = 2L;
        LocalDateTime now = LocalDateTime.now();

        saveReservationTime(RESERVATION_TIME_ID, now.toLocalTime());
        saveTheme(THEME_ID, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL);
        saveMember(MEMBER_ID, NAME, EMAIL, PASSWORD);
        saveReservation(reservationId, now.toLocalDate(), RESERVATION_TIME_ID, THEME_ID, MEMBER_ID);

        // when
        reservationDao.deleteById(reservationId);

        // then
        String sql = "select count(*) from reservation where id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, reservationId);
        assertThat(count).isZero();
    }

    @DisplayName("날짜와 시간, 테마로 존재하는 예약인지 확인한다.")
    @CsvSource(value = {
            "29,29,true",
            "28,29,false"
    })
    @ParameterizedTest
    void test8(int day1, int day2, boolean expected) {
        // given
        Long reservationId = 2L;
        LocalDateTime now = LocalDateTime.of(2025, 4, day1, 10, 0);

        saveReservationTime(RESERVATION_TIME_ID, now.toLocalTime());
        saveTheme(THEME_ID, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL);
        saveMember(MEMBER_ID, NAME, EMAIL, PASSWORD);
        saveReservation(reservationId, now.toLocalDate(), RESERVATION_TIME_ID, THEME_ID, MEMBER_ID);

        // when & then
        assertThat(
                reservationDao.existsByDateAndTimeIdAndThemeId(
                        LocalDate.of(2025, 4, day2), RESERVATION_TIME_ID, THEME_ID
                )
        ).isEqualTo(expected);
    }

    private ReservationTime saveReservationTime(Long id, LocalTime time) {
        String insertTimeSql = "insert into reservation_time (id, start_at) values (?, ?)";
        jdbcTemplate.update(insertTimeSql, id, time);

        return new ReservationTime(id, time);
    }

    private Theme saveTheme(Long id, String name, String description, String thumbnail) {
        String insertTimeSql = "insert into theme (id, name, description, thumbnail) values (?, ?, ?, ?)";
        jdbcTemplate.update(insertTimeSql, id, name, description, thumbnail);

        return new Theme(id, name, description, thumbnail);
    }

    private Member saveMember(Long id, String name, String email, String password) {
        String insertSql = "insert into member (id, name, email, password, role) values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, id, name, email, password, Role.MEMBER.name());

        return new Member(id, name, email, password, Role.MEMBER);
    }

    private void saveReservation(Long id, LocalDate date, Long timeId, Long themeId, Long memberId) {
        String sql = "insert into reservation (id, date, time_id, theme_id, member_id) values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, date, timeId, themeId, memberId);
    }
}
