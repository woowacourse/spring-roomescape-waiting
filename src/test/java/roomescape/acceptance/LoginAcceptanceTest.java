package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.BasicAcceptanceTest;

class LoginAcceptanceTest extends BasicAcceptanceTest {
    @TestFactory
    @DisplayName("회원가입하지 않은 이메일과 비밀번호로 로그인 할 시 예외를 발생시킨다")
    Stream<DynamicTest> moveNotReservationAndAdminPage() {
        return Stream.of(
                dynamicTest("회원가입 하지 않은 계정으로 로그인을 시도한다", () -> LoginTokenProvider.login("gsd", "401", 400)),
                dynamicTest("회원가입 하지 않은 계정으로 로그인을 시도한다", () -> LoginTokenProvider.login("sudal@wooteco.com", "401", 400)),
                dynamicTest("회원가입 하지 않은 계정으로 로그인을 시도한다", () -> LoginTokenProvider.login("email", "wootecoCrew6!", 400))
        );
    }

    @TestFactory
    @DisplayName("role이 USER인 Member는 Admin 페이지에 접속하지 못 한다")
    Stream<DynamicTest> moveNotAdminPageTest() {
        AtomicReference<String> userToken = new AtomicReference<>();
        return Stream.of(
                dynamicTest("role이 USER인 계정으로 로그인을 한다", () -> userToken.set(
                        LoginTokenProvider.login("member@wooteco.com", "wootecoCrew6!", 200))),
                dynamicTest("로그인한 계정의 이름을 확인한다", () -> loginCheck(userToken.get(), 200, "회원")),
                dynamicTest("admin 페이지에 접속한다", () -> moveToAdminPage(userToken.get(), 403)),
                dynamicTest("admin 예약 관리 페이지에 접속한다", () -> moveToReservationAdminPage(userToken.get(), 403)),
                dynamicTest("admin 시간 관리 페이지에 접속한다", () -> moveToTimeAdminPage(userToken.get(), 403)),
                dynamicTest("admin 테마 관리 페이지에 접속한다", () -> moveToThemeAdminPage(userToken.get(), 403)),
                dynamicTest("admin 예약 대기 관리 페이지에 접속한다", () -> moveToWaitingAdminPage(userToken.get(), 403)),
                dynamicTest("로그아웃 한다", () -> logout(200))
        );
    }

    @TestFactory
    @DisplayName("role이 ADMIN인 Member는 Admin 페이지에 접속가능하다")
    Stream<DynamicTest> moveAdminPageTest() {
        AtomicReference<String> adminToken = new AtomicReference<>();
        return Stream.of(
                dynamicTest("role이 ADMIN인 계정으로 로그인을 한다", () -> adminToken.set(
                        LoginTokenProvider.login("admin@wooteco.com", "wootecoCrew6!", 200))),
                dynamicTest("로그인한 계정의 이름을 확인한다", () -> loginCheck(adminToken.get(), 200, "운영자")),
                dynamicTest("admin 페이지에 접속한다", () -> moveToAdminPage(adminToken.get(), 200)),
                dynamicTest("admin 예약 관리 페이지에 접속한다", () -> moveToReservationAdminPage(adminToken.get(), 200)),
                dynamicTest("admin 시간 관리 페이지에 접속한다", () -> moveToTimeAdminPage(adminToken.get(), 200)),
                dynamicTest("admin 테마 관리 페이지에 접속한다", () -> moveToThemeAdminPage(adminToken.get(), 200)),
                dynamicTest("admin 예약 대기 관리 페이지에 접속한다", () -> moveToWaitingAdminPage(adminToken.get(), 200)),
                dynamicTest("로그아웃 한다", () -> logout(200))
        );
    }

    private void moveToAdminPage(String token, int expectedHttpCode) {
        RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void moveToReservationAdminPage(String token, int expectedHttpCode) {
        RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void moveToThemeAdminPage(String token, int expectedHttpCode) {
        RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void moveToTimeAdminPage(String token, int expectedHttpCode) {
        RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void moveToWaitingAdminPage(String token, int expectedHttpCode) {
        RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void logout(int expectedHttpCode) {
        RestAssured.given().log().all()
                .when().post("/logout")
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void loginCheck(String token, int expectedHttpCode, String expectedName) {
        Response response = RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        String responseName = response.jsonPath().getString("name");

        assertThat(responseName).isEqualTo(expectedName);
    }
}
