package roomescape.controller.page;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.ControllerTest;
import roomescape.service.auth.dto.LoginRequest;

import java.util.stream.Stream;

@Sql("/truncate-with-admin-and-guest.sql")
class AdminPageControllerTest extends ControllerTest {
    private String token;

    @DisplayName("Admin Page 홈화면 접근 성공 테스트")
    @TestFactory
    Stream<DynamicTest> responseAdminPage() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    token = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("어드민 홈화면에 들어간다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", token)
                            .when().get("/admin")
                            .then().log().all()
                            .assertThat().statusCode(HttpStatus.OK.value());
                })
        );
    }

    @DisplayName("Admin Reservation Page 접근 성공 테스트")
    @TestFactory
    Stream<DynamicTest> responseAdminReservationPage() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    token = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("어드민 예약 관리 페이지에 들어간다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", token)
                            .when().get("/admin/reservation")
                            .then().log().all()
                            .assertThat().statusCode(HttpStatus.OK.value());
                })
        );
    }

    @DisplayName("Admin Reservation Time Page 접근 성공 테스트")
    @TestFactory
    Stream<DynamicTest> responseReservationTimePage() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    token = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("어드민 시간 관리 페이지에 들어간다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", token)
                            .when().get("/admin/time")
                            .then().log().all()
                            .assertThat().statusCode(HttpStatus.OK.value());
                })
        );
    }

    @DisplayName("Admin Theme Page 접근 성공 테스트")
    @TestFactory
    Stream<DynamicTest> responseThemePage() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    token = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("어드민 테마 관리 페이지에 들어간다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", token)
                            .when().get("/admin/theme")
                            .then().log().all()
                            .assertThat().statusCode(HttpStatus.OK.value());
                })
        );
    }
}
