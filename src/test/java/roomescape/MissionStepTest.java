package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.controller.dto.SignupRequest;
import roomescape.member.service.AuthService;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;

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
                INSERT INTO member (name, email, role) VALUES (?, ?, ?)
                """, "솔라", adminEmail, "ADMIN");
        jdbcTemplate.update("""
                INSERT INTO account (password, member_id) VALUES (?, ?)
                """, passwordEncoder.encode(memberPassword), 2);

        memberToken = authService.login(new LoginRequest(memberEmail, memberPassword));
        adminToken = authService.login(new LoginRequest(adminEmail, adminPassword));
    }

    @Test
    void 회원가입을_통해_회원_정보를_저장하고_이메일과_비밀번호를_통해_로그인한다() {
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
    void 로그인_상태라면_로그인_정보를_확인할_수_있고_로그아웃_할_수_있다() {
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
    void 권한이_없다면_어드민_페이지에_접속할_수_없다() {
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
    void 권한이_없다면_예약들을_조회할_수_없다() {
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
    void reservations에_POST_요청_시_예약이_추가되고_DELETE_요청_시_각각_예약이_취소된다() {
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
    void JdbcTemplate로_DataSource와_Connection_확인_및_테이블_검증한다() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 데이터베이스에_예약_하나_추가_후_예약_조회_API를_통해_조회한_예약_수와_데이터베이스_쿼리를_통해_조회한_예약_수가_같은지_비교할_수_있다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "설명", "엄지손톱");

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                1, "2023-08-05", 1, 1);

        final List<ReservationWithStatusResponse> reservations = RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationWithStatusResponse.class);

        final Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    void 예약_추가_삭제_API를_활용하고_조회로_확인할_수_있다() {
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
    void 시간으로_API를_관리할_수_있다() {
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
    void 컨트롤러에_jdbcTemplate가_존재하지_않는다() {
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
