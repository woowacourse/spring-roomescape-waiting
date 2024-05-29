package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.model.member.Role;
import roomescape.util.TokenManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminControllerTest {

    private static final int INITIAL_RESERVATION_COUNT = 2;
    private static final int INITIAL_WAITING_COUNT = 2;
    private static final String LOGIN_USER_TOKEN = TokenManager.create(
            new MemberWithoutPassword(1L, "에버", "treeboss@gmail.com", Role.USER));
    private static final String LOGIN_ADMIN_TOKEN = TokenManager.create(
            new MemberWithoutPassword(2L, "관리자", "admin@gmail.com", Role.ADMIN));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert memberInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;
    private final SimpleJdbcInsert waitingInsertActor;

    @Autowired
    public AdminControllerTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
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
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("후에버", "whoever@gmail.com", "whoever123!", "USER");
        insertMember("관리자", "admin@gmail.com", "admin123!", "ADMIN");
        insertTime(LocalTime.of(1, 0));
        insertTheme("n1", "d1", "t1");
        insertReservation(LocalDate.of(3333, 1, 1), 1, 1, 1);
        insertReservation(LocalDate.of(3333, 1, 1), 2, 2, 2);
        insertWaiting(LocalDate.of(3333, 1, 1), 1, 1, 2);
        insertWaiting(LocalDate.of(3333, 1, 1), 1, 1, 3);
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE waiting RESTART IDENTITY");
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
    }

    private void insertTime(LocalTime startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
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

    @DisplayName("관리자가 어드민 API 접근에 시도할 경우 예외를 반환하지 않는다.")
    @Test
    void should_throw_exception_when_admin_contact() {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("일반 유저가 어드민 API 접근에 시도할 경우 예외를 반환한다.")
    @Test
    void should_not_throw_exception_when_user_contact() {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_USER_TOKEN)
                .body(new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(401);
    }

    // TODO: 쿠키가 존재하지 않는 경우 테스트

    @DisplayName("존재하는 예약이라면 예외를 반환하지 않고 예약을 삭제할 수 있다.")
    @Test
    void should_delete_reservation_when_reservation_exist() {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약 삭제 - id가 1 미만일 경우 예외를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-999"})
    void should_throw_exception_when_delete_by_invalid_id(String id) {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().delete("/admin/reservations/" + id)
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약 삭제 - id가 null일 경우 매퍼를 찾지 못하여 404 예외를 반환한다.")
    @Test
    void should_throw_exception_when_delete_by_id_null() {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().delete("/admin/reservations/")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예약 삭제 - 로그인이 되지 않은 경우 401 예외를 반환한다.")
    @Test
    void should_throw_exception_when_not_login() {
        RestAssured.given().log().all()
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예약이 삭제될 경우 해당 방탈출의 가장 높은 우선순위의 예약 대기를 자동으로 승인한다.")
    @Test
    void should_reserve_first_waiting_when_delete_reservation() {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAllReservations()).isEqualTo(INITIAL_RESERVATION_COUNT);
        assertThat(countAllWaiting()).isEqualTo(INITIAL_WAITING_COUNT - 1);
    }

    @DisplayName("전체 예약 대기를 조회한다.")
    @Test
    void should_get_all_reservation_waiting() {
        List<WaitingResponse> reservations = RestAssured.given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", WaitingResponse.class);

        assertThat(reservations).hasSize(INITIAL_WAITING_COUNT);
    }

    @DisplayName("존재하는 예약 대기라면 예약 대기를 삭제할 수 있다.")
    @Test
    void should_delete_reservation_waiting_when_reservation_waiting_exist() {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().delete("/admin/reservations/waiting/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAllWaiting()).isEqualTo(INITIAL_WAITING_COUNT - 1);
    }

    private Integer countAllReservations() {
        return jdbcTemplate.queryForObject("SELECT count(id) from reservation", Integer.class);
    }

    private Integer countAllWaiting() {
        return jdbcTemplate.queryForObject("SELECT count(id) from waiting", Integer.class);
    }
}
