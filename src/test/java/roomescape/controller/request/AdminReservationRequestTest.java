package roomescape.controller.request;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.model.member.Role;
import roomescape.util.TokenManager;

import java.time.LocalDate;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminReservationRequestTest {

    private static final String LOGIN_ADMIN_TOKEN = TokenManager.create(
            new MemberWithoutPassword(2L, "관리자", "admin@gmail.com", Role.ADMIN));

    @DisplayName("요청된 데이터의 날짜가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date_null() {
        AdminReservationRequest request = new AdminReservationRequest(null, 1L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 날짜가 과거일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date_past() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now().minusDays(1), 1L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 시간 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), null, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 시간 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId_range() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 0L, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 테마 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, null, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 테마 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId_range() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 0L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 사용자 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_memberId() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, null);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 사용자 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_memberId_range() {
        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 0L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
