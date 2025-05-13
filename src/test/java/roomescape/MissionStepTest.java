package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.controller.dto.SignupRequest;
import roomescape.member.domain.Role;
import roomescape.member.service.AuthService;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservation.controller.dto.ReservationWebResponse;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String memberToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        String memberEmail = "siso@naver.com";
        String memberPassword = "1234";
        String adminEmail = "solar@naver.com";
        String adminPassword = "1234";

        authService.signup(new SignupRequest(memberEmail, memberPassword, "시소"));
        jdbcTemplate.update("""
                INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)
                """, "솔라", adminEmail, passwordEncoder.encode(memberPassword), Role.ADMIN.name());

        memberToken = authService.login(new LoginRequest(memberEmail, memberPassword));
        adminToken = authService.login(new LoginRequest(adminEmail, adminPassword));
    }

    @Test
    @DisplayName("회원가입을 통해 회원 정보를 저장하고, 이메일과 비밀번호를 통해 로그인한다")
    void signup() {
        final Map<String, String> params = new HashMap<>();
        params.put("name", "gangsan");
        params.put("email", "gangsan@gmail.com");
        params.put("password", "1234");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/signup")
                .then().log().all()
                .statusCode(200)
                .body("id", is(3));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("로그인 상태라면 로그인 정보를 확인할 수 있고, 로그아웃 할 수 있다")
    void login() {
        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", is("시소"));

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .when().post("/logout")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("권한이 없다면 어드민 페이지에 접속할 수 없다")
    void first() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("권한이 없다면 예약들을 조회할 수 없다")
    void second() {
        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("3단계: localhost:8080/reservations 에 POST 요청 시 예약이 추가되고, DELETE 요청 시 각각 예약이 취소된다")
    void third() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "설명", "엄지손톱");

        final Map<String, String> params = new HashMap<>();
        params.put("date", "2025-08-05");
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", memberToken)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(params)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("JdbcTemplate로 DataSource객체에 접근할 수 있다" +
            "DataSource로 Connection 확인할 수 있다" +
            "Connection로 데이터베이스, 테이블 이름 검증할 수 있다")
    void fourth() {
        try (final Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("데이터베이스에 예약 하나 추가 후 예약 조회 API를 통해 조회한 예약 수와 데이터베이스 쿼리를 통해 조회한 예약 수가 같은지 비교할 수 있다")
    void fifth() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "설명", "엄지손톱");

        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                1, "2023-08-05", 1, 1);

        final List<ReservationWebResponse> reservations = RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationWebResponse.class);

        final Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    @DisplayName("예약 추가/삭제 API를 활용하고, 조회로 확인할 수 있다")
    void sixth() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                "10:00");

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "설명", "엄지손톱");

        final Map<String, String> params = new HashMap<>();
        params.put("date", "2025-08-05");
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", memberToken)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        final Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        final Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    @DisplayName("시간으로 API를 관리할 수 있다")
    void seventh() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                "10:00");

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("컨트롤러에 jdbcTemplate가 존재하지 않는다")
    void ninth() {
        boolean isJdbcTemplateInjected = false;

        for (final Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
