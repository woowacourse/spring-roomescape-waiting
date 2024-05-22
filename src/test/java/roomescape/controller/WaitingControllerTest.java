package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.controller.request.WaitingRequest;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingControllerTest {

    private static final int INITIAL_WAITING_COUNT = 15;
    private static final AuthDto userDto = new AuthDto("treeboss@gmail.com", "treeboss123!");

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert memberInsertActor;
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
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.minusDays(i), 1L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.minusDays(i), 2L, 1L, 1L));
        IntStream.range(0, 5).forEach(i -> insertWaiting(now.minusDays(i), 3L, 1L, 1L));
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
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

    private void insertWaiting(LocalDate date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        waitingInsertActor.execute(parameters);
    }

    @DisplayName("예약을 추가할 수 있다.")
    @Test
    void should_insert_reservation() {
        String token = authService.createToken(userDto); // TODO: can change to TokenManager.create()
        WaitingRequest request = new WaitingRequest(LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/waiting/" + (INITIAL_WAITING_COUNT + 1));

        assertThat(countAllReservations()).isEqualTo(INITIAL_WAITING_COUNT + 1);
    }

    private Integer countAllReservations() {
        return jdbcTemplate.queryForObject("SELECT count(id) from waiting", Integer.class);
    }
}
