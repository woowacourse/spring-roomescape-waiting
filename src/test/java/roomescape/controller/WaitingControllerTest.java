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
import roomescape.controller.request.WaitingRequest;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.model.member.Role;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;
import roomescape.util.TokenManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingControllerTest {

    private static final int INITIAL_WAITING_COUNT = 15;
    private static final String LOGIN_TOKEN = TokenManager.create(
            new MemberWithoutPassword(2L, "관리자", "admin@gmail.com", Role.ADMIN));

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert memberInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;
    private final SimpleJdbcInsert waitingInsertActor;

    @Autowired
    public WaitingControllerTest(JdbcTemplate jdbcTemplate, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
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
        insertMember("후에버", "whoever@gmail.com", "whoever123!", "USER");

        LocalDate now = LocalDate.now();
        IntStream.range(0, 5).forEach(i -> insertReservation(now.minusDays(i), 1L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertReservation(now.minusDays(i), 2L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertReservation(now.minusDays(i), 3L, 1L, 1L));
        insertReservation(now.plusDays(1), 1L, 1L, 1L);

        IntStream.range(0, 5).forEach(i -> insertWaiting(now.minusDays(i), 1L, 1L, 2L));
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.minusDays(i), 2L, 1L, 2L));
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.minusDays(i), 3L, 1L, 2L));
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

    @DisplayName("예약 대기를 추가할 수 있다.")
    @Test
    void should_insert_reservation_waiting() {
        String token = TokenManager.create(new MemberWithoutPassword(3L, "포에버", "forever@gmail.com", Role.USER));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new WaitingRequest(LocalDate.now().plusDays(1), 1L, 1L))
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/waiting/" + (INITIAL_WAITING_COUNT + 1));

        assertThat(countAll()).isEqualTo(INITIAL_WAITING_COUNT + 1);
    }

    @DisplayName("예약 대기 삭제 - 성공")
    @Test
    void should_delete_reservation_waiting_when_reservation_waiting_exist() {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_TOKEN)
                .when().delete("/reservations/waiting/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAll()).isEqualTo(INITIAL_WAITING_COUNT - 1);
    }

    @DisplayName("예약 대기 삭제 - id가 1 미만일 경우 예외를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-999"})
    void should_throw_exception_when_delete_by_invalid_id(String id) {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_TOKEN)
                .when().delete("/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약 대기 삭제 - id가 null일 경우 매퍼를 찾지 못하여 404 예외를 반환한다.")
    @Test
    void should_throw_exception_when_delete_by_id_null() {
        RestAssured.given().log().all()
                .cookie("token", LOGIN_TOKEN)
                .when().delete("/reservations/waiting/")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예약 대기 삭제 - 로그인이 되지 않은 경우 401 예외를 반환한다.")
    @Test
    void should_throw_exception_when_delete_with_not_login() {
        RestAssured.given().log().all()
                .when().delete("/reservations/waiting/1")
                .then().log().all()
                .statusCode(401);
    }

    private Integer countAll() {
        return jdbcTemplate.queryForObject("SELECT count(id) from waiting", Integer.class);
    }
}
