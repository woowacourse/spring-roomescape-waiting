package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.MemberWaitingResponse;
import roomescape.controller.response.WaitingResponse;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingControllerTest {

    private static final int INITIAL_WAITING_COUNT = 10;
    private static final AuthDto userDto = new AuthDto("treeboss@gmail.com", "treeboss123!");

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

        insertMember("아토", "atto@gmail.com", "atto123!", "USER");
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("우테코", "wtc@gmail.com", "wtc123!", "ADMIN");

        LocalDate now = LocalDate.now();
        IntStream.range(0, 5).forEach(i -> insertReservation(now.plusDays(i), 1L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertReservation(now.plusDays(i), 2L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertReservation(now.plusDays(i), 3L, 1L, 1L));

        LocalDateTime nowDateTime = LocalDateTime.now();
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.plusDays(i), 1L, 1L, 2L, nowDateTime.plusHours(2)));
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.plusDays(i), 1L, 1L, 3L, nowDateTime.plusHours(1)));
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

    private void insertWaiting(LocalDate date, long timeId, long themeId, long memberId, LocalDateTime createdAt) {
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        parameters.put("created_at", createdAt);
        waitingInsertActor.execute(parameters);
    }

    @DisplayName("전체 대기를 조회한다.")
    @Test
    void should_get_all_waitings() {
        List<WaitingResponse> waitings = RestAssured.given().log().all()
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", WaitingResponse.class);

        assertThat(waitings).hasSize(INITIAL_WAITING_COUNT);
    }

    @DisplayName("대기를 추가할 수 있다.")
    @Test
    void should_insert_waiting() {
        String token = authService.createToken(userDto);
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 2L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/waitings/" + (INITIAL_WAITING_COUNT + 1));

        assertThat(countAllWaitings()).isEqualTo(INITIAL_WAITING_COUNT + 1);
    }

    @DisplayName("존재하는 대기라면 대기를 삭제할 수 있다.")
    @Test
    void should_delete_waiting_when_waiting_exist() {
        RestAssured.given().log().all()
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAllWaitings()).isEqualTo(INITIAL_WAITING_COUNT - 1);
    }

    @DisplayName("대기 삭제 - id가 1 미만일 경우 예외를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-999"})
    void should_throw_exception_when_delete_by_invalid_id(String id) {
        RestAssured.given().log().all()
                .when().delete("/waitings/" + id)
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("자신의 대기를 조회한다.")
    @Test
    void should_find_waitings_of_member() {
        String token = authService.createToken(userDto);

        List<MemberWaitingResponse> responses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/waitings/mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MemberWaitingResponse.class);

        assertThat(responses).hasSize(5);
    }

    private Integer countAllWaitings() {
        return jdbcTemplate.queryForObject("SELECT count(id) from waiting", Integer.class);
    }
}
