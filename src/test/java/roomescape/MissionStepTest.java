package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.global.auth.dto.CheckLoginResponse;
import roomescape.global.auth.dto.LoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SignupRequest;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.fixture.TestFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
public class MissionStepTest {

    private static final String USER_EMAIL = "user@gmail.com";
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String PASSWORD = "password";
    private static final String futureDate = TestFixture.makeFutureDate().toString();
    private static final String TOKEN = "token";

    @Nested
    class AdminTest {

        @Autowired
        private JdbcTemplate jdbcTemplate;
        @Autowired
        private ReservationController reservationController;

        @Test
        void step1_accessAdminPage() {
            String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

            RestAssured.given().log().all()
                    .cookie(TOKEN, authToken)
                    .when().get("/admin")
                    .then().log().all()
                    .statusCode(200);
        }

        @Test
        void step2_accessAdminReservationPage() {
            String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

            RestAssured.given().log().all()
                    .cookie(TOKEN, authToken)
                    .when().get("/admin/reservation")
                    .then().log().all()
                    .statusCode(200);
        }

        @Test
        void step3_createAndDeleteReservation() {
            String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

            createReservationTime();
            createTheme("추리");
            createUserReservation(1L);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));

            RestAssured.given().log().all()
                    .cookie(TOKEN, authToken)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @Test
        void step4_applyDatabase() {
            try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
                assertThat(connection).isNotNull();
                assertThat(connection.getCatalog()).isEqualTo("DATABASE");
                assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void step5_getReservationWithDatabase() {
            createReservationTime();
            createTheme("추리");
            signup();

            jdbcTemplate.update("INSERT INTO reservation (date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?)",
                    futureDate, "1", "1", "1");

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200);

            Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void step6_addReservationWithDatabase() {
            String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

            createReservationTime();
            createTheme("추리");
            createUserReservation(1L);

            Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
            assertThat(count).isEqualTo(1);

            RestAssured.given().log().all()
                    .cookie(TOKEN, authToken)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
            assertThat(countAfterDelete).isEqualTo(0);
        }

        @Test
        void step7_timeAPIFeature() {
            String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);
            createReservationTime();

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));

            RestAssured.given().log().all()
                    .cookie(TOKEN, authToken)
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void step8_schemaModification() {
            createReservationTime();
            createTheme("추리");
            createUserReservation(1L);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void step9_controllerDoesNotHasDatabaseLogic() {
            boolean isJdbcTemplateInjected = false;

            for (Field field : reservationController.getClass().getDeclaredFields()) {
                if (field.getType().equals(JdbcTemplate.class)) {
                    isJdbcTemplateInjected = true;
                    break;
                }
            }
            assertThat(isJdbcTemplateInjected).isFalse();
        }
    }

    @Nested
    class UserTest {

        private static final String ADMIN_TOKEN = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);
        private static final String USER_TOKEN = loginAndGetAuthToken(USER_EMAIL, PASSWORD);

        @Test
        void step1_exceptionHandle() {
            Map<String, String> reservationTime = new HashMap<>();
            reservationTime.put("startAt", "10 00");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(reservationTime)
                    .cookie(TOKEN, ADMIN_TOKEN)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void step2_createAndDeleteTheme() {
            createTheme("추리");
            findThemesBySize(1);

            RestAssured.given().log().all()
                    .cookie(TOKEN, ADMIN_TOKEN)
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(204);
            findThemesBySize(0);
        }

        @Test
        void step3_findAvailableReservations() {
            createReservationTime();
            createTheme("추리");

            LocalDate now = LocalDate.now();
            RestAssured.given().log().all()
                    .when().queryParams("date", now.toString(), "themeId", 1L).get("/times/available")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @Test
        void step3_findPopularTheme() {
            createReservationTime();
            createTheme("추리1");
            createTheme("추리2");
            createTheme("추리3");
            createTheme("추리4");
            createTheme("추리5");
            createTheme("추리6");
            createTheme("추리7");
            createTheme("추리8");
            createTheme("추리9");
            createTheme("추리10");
            createTheme("추리11");
            createTheme("추리12");
            findThemesBySize(12);

            RestAssured.given().log().all()
                    .when().get("/themes/popular")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(10));
        }

        @Test
        void step4_signup() {
            signup();
        }

        @Test
        void step4_login() {
            RestAssured.given().log().all()
                    .body(new LoginRequest(USER_EMAIL, PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(200);
        }


        @Test
        void step4_logout() {
            RestAssured.given().log().all()
                    .body(new LoginRequest(USER_EMAIL, PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie(TOKEN, USER_TOKEN)
                    .when().post("/logout")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void step4_loginCheck() {
            CheckLoginResponse checkLoginResponse = RestAssured.given().log().all()
                    .body(new LoginRequest(USER_EMAIL, PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie(TOKEN, USER_TOKEN)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(CheckLoginResponse.class);

            assertThat(checkLoginResponse.name()).isEqualTo("User");
        }

        @Test
        void step5_admin_reservation() {
            createReservationTime();
            createTheme("추리");

            Map<String, Object> reservation = new HashMap<>();
            reservation.put("date", futureDate);
            reservation.put("timeId", 1);
            reservation.put("themeId", 1);
            reservation.put("memberId", 2);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(reservation)
                    .cookie(TOKEN, ADMIN_TOKEN)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(201);
        }

        @Test
        void step5_findAllUsers() {
            List<MemberResponse> memberResponses = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(TOKEN, ADMIN_TOKEN)
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(new TypeRef<>() {
                    });
            assertThat(memberResponses.size()).isEqualTo(1);
        }

        @Test
        void step6_responseUnAuthorizedWhenUserAccessAdminPage() {
            RestAssured.given().log().all()
                    .when().get("/admin/reservation")
                    .then().log().all()
                    .statusCode(401);
        }

        @Test
        void step6_responseForbiddenWhenUserAccessAdminPage() {
            RestAssured.given().log().all()
                    .cookie(TOKEN, USER_TOKEN)
                    .when().get("/admin/reservation")
                    .then().log().all()
                    .statusCode(403);
        }

        @Test
        void step7_filterReservations() {
            createReservationTime();
            createTheme("추리");
            createTheme("로맨스");
            createUserReservation(1L);
            createUserReservation(2L);

            List<ReservationResponse> reservationsFilteredByThemeId = RestAssured.given().log().all()
                    .when().queryParams("themeId", 1L, "memberId", 2L, "dateFrom", futureDate,
                            "dateTo", TestFixture.makeFutureDate().plusDays(1).toString())
                    .get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(new TypeRef<>() {
                    });
            assertThat(reservationsFilteredByThemeId.size()).isEqualTo(1);
        }
    }

    private static String loginAndGetAuthToken(final String email, final String password) {
        return RestAssured.given().log().all()
                .body(new LoginRequest(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie(TOKEN);
    }

    private void signup() {
        RestAssured.given().log().all()
                .body(new SignupRequest("testuser@gmail.com", PASSWORD, "testUser"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    private void createUserReservation(final Long themeId) {
        String authToken = loginAndGetAuthToken(USER_EMAIL, PASSWORD);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", futureDate);
        reservation.put("timeId", 1);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .cookie(TOKEN, authToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    private void createTheme(final String name) {
        String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

        Map<String, String> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", "셜록 with Danny");
        theme.put("thumbnail", "image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .cookie(TOKEN, authToken)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    private void createReservationTime() {
        String authToken = loginAndGetAuthToken(ADMIN_EMAIL, PASSWORD);

        Map<String, String> reservationTime = new HashMap<>();
        reservationTime.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .cookie(TOKEN, authToken)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    private void findThemesBySize(final int size) {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(size));
    }
}
