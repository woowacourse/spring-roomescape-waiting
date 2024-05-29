package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.OwnReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.controller.response.ReservationTimeInfoResponse;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.model.member.Role;
import roomescape.util.TokenManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationControllerTest {

    private static final int INITIAL_TIME_COUNT = 5;
    private static final int INITIAL_RESERVATION_COUNT = 15;
    private static final String LOGIN_TOKEN = TokenManager.create(
            new MemberWithoutPassword(1L, "에버", "treeboss@gmail.com", Role.USER));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert memberInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;
    private final SimpleJdbcInsert waitingInsertActor;

    @Autowired
    public ReservationControllerTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        this.reservationInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        this.waitingInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        IntStream.range(1, 6).forEach(i -> insertReservationTime(i + ":00"));
        IntStream.range(0, 20).forEach(i -> insertTheme("n" + i, "d" + i, "t" + i));

        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("우테코", "wtc@gmail.com", "wtc123!", "ADMIN");

        LocalDate now = LocalDate.now();
        IntStream.range(0, 5).forEach(i -> insertReservation(now.minusDays(i), 1L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertReservation(now.minusDays(i), 2L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertReservation(now.minusDays(i), 3L, 1L, 1L));

        insertWaiting(now.minusDays(0), 1L, 1L, 2L);
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE waiting RESTART IDENTITY");
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
    }

    private void insertReservation(LocalDate date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        reservationInsertActor.execute(parameters);
    }

    private void insertWaiting(LocalDate date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        waitingInsertActor.execute(parameters);
    }

    @DisplayName("전체 예약을 조회한다.")
    @Test
    void should_get_all_reservations() {
        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ReservationResponse.class);

        assertThat(reservations).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @DisplayName("예약을 추가할 수 있다.")
    @Test
    void should_insert_reservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_TOKEN)
                .body(new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/" + (INITIAL_RESERVATION_COUNT + 1));

        assertThat(countAllReservations()).isEqualTo(INITIAL_RESERVATION_COUNT + 1);
    }

    @DisplayName("특정 날짜와 테마에 따른 모든 시간의 예약 가능 여부를 확인 - 테마 id가 null 또는 1 미만일 경우 예외를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1, -999})
    void should_throw_exception_when_get_by_invalid_themeId(Long themeId) {
        String date = LocalDate.now().minusDays(1).toString();
        RestAssured.given().log().all()
                .param("date", date)
                .param("themeId", themeId)
                .when().get("/reservations/times")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("특정 날짜와 테마에 따른 모든 시간의 예약 가능 여부를 확인한다.")
    @Test
    void should_get_reservations_with_book_state_by_date_and_theme() {
        Long themeId = 1L;
        String date = LocalDate.now().minusDays(1).toString();
        List<ReservationTimeInfoResponse> times = RestAssured.given().log().all()
                .param("themeId", themeId)
                .param("date", date)
                .when().get("/reservations/times")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ReservationTimeInfoResponse.class);

        assertAll(
                () -> assertThat(times).hasSize(INITIAL_TIME_COUNT),
                () -> assertThat(times.get(0).getIsBooked()).isTrue(),
                () -> assertThat(times.get(0).getTimeId()).isEqualTo(1),
                () -> assertThat(times.get(1).getIsBooked()).isTrue(),
                () -> assertThat(times.get(1).getTimeId()).isEqualTo(2),
                () -> assertThat(times.get(2).getIsBooked()).isTrue(),
                () -> assertThat(times.get(2).getTimeId()).isEqualTo(3),
                () -> assertThat(times.get(3).getIsBooked()).isFalse(),
                () -> assertThat(times.get(3).getTimeId()).isEqualTo(4),
                () -> assertThat(times.get(4).getIsBooked()).isFalse(),
                () -> assertThat(times.get(4).getTimeId()).isEqualTo(5));
    }

    @DisplayName("날짜, 테마, 사용자 조건으로 예약을 검색한다.")
    @Test
    void should_get_reservations_by_date_and_theme_and_member() {
        RestAssured.given().log().all()
                .param("memberId", 1L)
                .param("themeId", 1L)
                .param("from", "2024-04-30")
                .param("to", "2024-05-10")
                .get("/reservations/filter")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ReservationResponse.class);
    }

    @DisplayName("자신의 예약을 조회한다.") // TODO: add waiting test case
    @Test
    void should_find_reservations_of_member() {
        List<OwnReservationResponse> responses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_TOKEN)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", OwnReservationResponse.class);

        assertThat(responses).hasSize(15);
    }

    private Integer countAllReservations() {
        return jdbcTemplate.queryForObject("SELECT count(id) from reservation", Integer.class);
    }
}
