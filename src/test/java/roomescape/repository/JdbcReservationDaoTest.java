package roomescape.repository;

import static java.time.LocalDate.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.model.Role.MEMBER;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcReservationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcInsert reservationTimeInsertActor;
    private SimpleJdbcInsert reservationInsertActor;
    private SimpleJdbcInsert themeInsertActor;
    private SimpleJdbcInsert userInsertActor;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        reservationTimeInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        reservationInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        themeInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        userInsertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        insertReservationTime("10:00");
        insertReservationTime("11:00");

        insertTheme("에버", "공포", "공포.jpg");
        insertTheme("배키", "스릴러", "스릴러.jpg");

        insertUser("브라운", MEMBER, "brown@email.com", "1111");
        insertUser("리사", MEMBER, "lisa@email.com", "2222");

        insertReservation("2023-08-05", "1", "1", "1");
        insertReservation("2023-08-01", "2", "2", "2");
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        reservationTimeInsertActor.execute(parameters);
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertUser(String name, Role role, String email, String password) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("role", role.name());
        parameters.put("email", email);
        parameters.put("password", password);
        userInsertActor.execute(parameters);
    }

    private void insertReservation(String date, String timeId, String themeId, String userId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("user_id", userId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void should_get_all_reservations() {
        List<Reservation> reservations = reservationDao.getAllReservations();
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("주어진 조건에 따라 예약을 검색한다.")
    @Test
    void should_search_reservation_by_condition() {
        insertReservation(now().plusDays(3).toString(), "1", "1", "1");
        insertReservation(now().plusDays(4).toString(), "2", "1", "1");
        insertReservation(now().plusDays(5).toString(), "2", "1", "1");

        LocalDate from = now().plusDays(2);
        LocalDate to = now().plusDays(5);
        List<Reservation> reservations = reservationDao.searchReservation(1L, 1L, from, to);

        assertThat(reservations).hasSize(3);
    }

    @DisplayName("조회한 예약에 예약 시간이 존재한다.")
    @Test
    void should_get_reservation_times() {
        List<Reservation> reservations = reservationDao.getAllReservations();
        assertThat(reservations.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("예약을 추가한다")
    @Test
    void should_add_reservation() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "에버", "공포", "공포.jpg");
        User user = new User(1L, "썬", MEMBER, "sun@email.com", "1234");
        Reservation reservation =
                new Reservation(LocalDate.of(2024, 9, 1), reservationTime, theme, user);

        reservationDao.addReservation(reservation);

        Integer count = jdbcTemplate.queryForObject("select count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void should_delete_reservation() {
        reservationDao.deleteReservation(1);
        Integer count = jdbcTemplate.queryForObject("select count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("아이디가 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        long count = reservationDao.countReservationById(1);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("아이디가 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_id_not_exist() {
        long count = reservationDao.countReservationById(100000000);
        assertThat(count).isZero();
    }

    @DisplayName("특정 날짜와 테마에 해당하는 시간을 조회한다.")
    @Test
    void should_get_reservation_times_when_date_and_theme_given() {
        LocalDate date = LocalDate.of(2023, 8, 5);
        List<ReservationTime> times = reservationDao.findReservationTimeByDateAndTheme(date, 1);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(times).hasSize(1);
            softAssertions.assertThat(times).containsExactly(new ReservationTime(1L, LocalTime.of(10, 0)));
        });
    }
}
