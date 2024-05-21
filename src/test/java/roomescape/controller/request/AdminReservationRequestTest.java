package roomescape.controller.request;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminReservationRequestTest {

    private static final AuthDto adminDto = new AuthDto("admin@gmail.com", "admin123!");

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;
    private final SimpleJdbcInsert memberInsertActor;

    @Autowired
    public AdminReservationRequestTest(JdbcTemplate jdbcTemplate, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("관리자", "admin@gmail.com", "admin123!", "ADMIN");
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
    }

    @DisplayName("요청된 데이터의 날짜가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date_null() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(null, 1L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 날짜가 과거일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date_past() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now().minusDays(1), 1L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 시간 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), null, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 시간 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId_range() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 0L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 테마 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, null, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 테마 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId_range() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 0L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 사용자 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_memberId() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, null);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 사용자 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_memberId_range() {
        String token = authService.createToken(adminDto);
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 0L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
