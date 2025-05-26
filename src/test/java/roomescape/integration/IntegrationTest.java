package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class IntegrationTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, String> reservationTime = new HashMap<>();
    private static final Map<String, String> theme = new HashMap<>();

    private static final Map<String, String> member = new HashMap<>();
    private static final Map<String, Object> authOfMember = new HashMap<>();
    private static final Map<String, Object> anotherMember = new HashMap<>();
    private static final Map<String, Object> anotherAuthOfMember = new HashMap<>();

    private static final Map<String, Object> reservation = new HashMap<>();
    private static final Map<String, Object> anotherReservation = new HashMap<>();

    private static final Map<String, Object> waiting = new HashMap<>();

    private static final Map<String, String> admin = new HashMap<>();
    private static final Map<String, Object> authOfAdmin = new HashMap<>();
    private static final Map<String, Object> adminReservation = new HashMap<>();

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        setUpTime();
        setUpTheme();
        setUpMemberAndLogin();
        setUpReservation();
        setUpWaiting();
        setUpAdminAndLogin();
        setUpAdminReservation();
    }

    private void setUpTime() {
        reservationTime.put("startAt", "10:00");
    }

    private void setUpTheme() {
        theme.put("name", "테마1");
        theme.put("description", "설명1");
        theme.put("thumbnail", "썸네일1");
    }

    private void setUpMemberAndLogin() {
        member.put("name", "브라운");
        member.put("email", "test@email.com");
        member.put("password", "pass1");

        authOfMember.put("email", "test@email.com");
        authOfMember.put("password", "pass1");

        anotherMember.put("name", "제로");
        anotherMember.put("email", "test2@email.com");
        anotherMember.put("password", "pass2");

        anotherAuthOfMember.put("email", "test2@email.com");
        anotherAuthOfMember.put("password", "pass2");
    }

    private void setUpReservation() {
        reservation.put("date", "2025-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        anotherReservation.put("date", "2025-08-06");
        anotherReservation.put("timeId", 1);
        anotherReservation.put("themeId", 1);
    }

    private void setUpWaiting() {
        waiting.put("reservationId", 2);
        waiting.put("date", "2025-08-06");
        waiting.put("timeId", 1);
        waiting.put("themeId", 1);
        waiting.put("memberId", 1);
        waiting.put("rank", 1);
    }

    private void setUpAdminAndLogin() {
        admin.put("name", "듀이");
        admin.put("email", "admin@email.com");
        admin.put("password", "admin");

        authOfAdmin.put("email", "admin@email.com");
        authOfAdmin.put("password", "admin");
    }

    private void setUpAdminReservation() {
        adminReservation.put("date", "2025-08-05");
        adminReservation.put("timeId", 1);
        adminReservation.put("themeId", 1);
        adminReservation.put("memberId", 1);
    }

    @Nested
    class ReservationTime {

        @Test
        void 예약_시간을_생성_조회_삭제한다() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(reservationTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1));

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 예약시간_생성시_이미_존재하는_예약시간이면_예외를_응답한다() {
            givenCreatedReservationTime();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(reservationTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 예약_시간_삭제시_존재하지_않는_예약시간이면_예외를_응답한다() {
            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
        @Test
        void 예약_시간_삭제시_이미_예약이_존재하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }
    }

    @Nested
    class Theme {

        @Test
        void 테마를_생성_조회_삭제한다() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(theme)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());

            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1));

            RestAssured.given().log().all()
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 테마_삭제시_존재하지_않는_테마면_예외를_응답한다() {
            RestAssured.given().log().all()
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
        @Test
        void 테마_삭제시_이미_예약이_존재하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();

            RestAssured.given().log().all()
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }
    }

    @Nested
    class Reservation {

        @Test
        void 사용자가_방탈출_예약을_생성_조회_삭제한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            String token = givenMemberLoginToken();

            // 생성
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(reservation)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());

            // 조회
            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1));

            // 삭제
            RestAssured.given().log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(0));
        }

        @Test
        void 관리자가_방탈출_예약을_생성한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedAdmin();
            String token = givenAdminLoginToken();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(adminReservation)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());
        }

        @Test
        void 방탈출_예약_목록을_응답한다() {
            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(0));
        }

        @Test
        void 이미_예약된_방탈출_예약을_생성하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            String token = givenMemberLoginToken();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(reservation)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 과거_방탈출_예약을_생성하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            reservation.put("date", "2024-08-06");
            givenCreatedReservation();
            String token = givenMemberLoginToken();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(reservation)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 예약_삭제시_존재하지_않는_예약이면_예외를_응답한다() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 사용자의_예약과_예약대기_목록을_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            givenCreatedWaiting();

            String token = givenMemberLoginToken();
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().get("/reservations-mine")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(2));
        }
    }

    @Nested
    class Member {

        @Test
        void 전체_사용자_목록을_응답한다() {
            givenCreatedMember();

            RestAssured.given().log().all()
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(2));
        }
    }

    @Nested
    class Auth {

        @Test
        void 로그인_성공시_토큰을_응답한다() {
            givenCreatedMember();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(authOfMember)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .cookie("token", notNullValue());
        }

        @Test
        void 존재하지_않는_이메일로_로그인하면_예외를_응답한다() {
            givenCreatedMember();
            authOfMember.put("email", "notExistEmail@email.com");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(authOfMember)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 잘못된_비밀번호로_로그인하면_예외를_응답한다() {
            givenCreatedMember();
            authOfMember.put("password", "incorrectPassword");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(authOfMember)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }
    }

    @Nested
    class Waiting {

        @Test
        void 사용자가_예약대기를_생성_삭제한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            String token = givenMemberLoginToken();

            // 생성
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(waiting)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());

            // 삭제
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().delete("/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 사용자가_이미_예약한_예약에_대기를_생성하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            waiting.put("reservationId", 1);
            waiting.put("date", "2025-08-05");
            String token = givenMemberLoginToken();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(waiting)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 사용자가_이미_대기한_예약에_대기를_생성하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            givenCreatedWaiting();
            String token = givenMemberLoginToken();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", token)
                    .body(waiting)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 사용자가_내_예약대기가_아닌_예약대기를_삭제하면_예외를_응답한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            givenCreatedWaiting();
            String token = givenAnotherMemberLoginToken();

            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().delete("/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 사용자가_예약을_취소하면_1순위_예약대기가_삭제된다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            givenCreatedWaiting();
            givenCreatedAdmin();
            String token = givenAnotherMemberLoginToken();
            String adminToken = givenAdminLoginToken();

            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().delete("/reservations/2")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            RestAssured.given().log().all()
                    .cookie("token", adminToken)
                    .when().get("/admin/reservations/waitings")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(0));
        }

        @Test
        void 관리자가_예약대기를_조회_삭제한다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            givenCreatedWaiting();
            givenCreatedAdmin();
            String token = givenAdminLoginToken();

            // 조회
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .body(waiting)
                    .when().get("/admin/reservations/waitings")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1));

            // 삭제
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().delete("/admin/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void 관리자가_예약을_거절하면_1순위_예약대기가_삭제된다() {
            givenCreatedReservationTime();
            givenCreatedTheme();
            givenCreatedMember();
            givenCreatedReservation();
            givenCreatedAnotherReservation();
            givenCreatedWaiting();
            givenCreatedAdmin();
            String token = givenAdminLoginToken();

            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().delete("/reservations/2")
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().get("/admin/reservations/waitings")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(0));
        }
    }

    private void givenCreatedReservationTime() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times");
    }

    private void givenCreatedTheme() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/themes");
    }

    private void givenCreatedMember() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/members");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(anotherMember)
                .when().post("/members");
    }

    private String givenMemberLoginToken() {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(authOfMember)
                .when().post("/login")
                .then()
                .extract().response().cookie("token");
    }

    private String givenAnotherMemberLoginToken() {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(anotherAuthOfMember)
                .when().post("/login")
                .then()
                .extract().response().cookie("token");
    }

    private void givenCreatedReservation() {
        String token = givenMemberLoginToken();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when().post("/reservations");
    }

    private void givenCreatedAnotherReservation() {
        String token = givenAnotherMemberLoginToken();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(anotherReservation)
                .when().post("/reservations");
    }

    private void givenCreatedWaiting() {
        String token = givenMemberLoginToken();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(waiting)
                .when().post("/reservations/waitings");
    }

    private void givenCreatedAdmin() {
        jdbcTemplate.update("INSERT INTO member (name, role, email, password) VALUES (?, ?, ?, ?)",
                admin.get("name"), "ADMIN", admin.get("email"), admin.get("password"));
    }

    private String givenAdminLoginToken() {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(authOfAdmin)
                .when().post("/login")
                .then()
                .extract().response().cookie("token");
    }
}
