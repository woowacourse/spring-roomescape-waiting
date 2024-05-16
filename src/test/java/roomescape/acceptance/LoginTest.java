package roomescape.acceptance;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class LoginTest extends AcceptanceTest {

    private String accessToken;

    @DisplayName("admin 권한으로 로그인 한다.")
    @TestFactory
    Stream<DynamicTest> loginWithAdmin() {
        return Stream.of(
                dynamicTest("로그인 하여 토큰을 발급받는다.", () -> {
                    accessToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(ADMIN_TOKEN_REQUEST)
                            .when().post("/login")
                            .then().log().all()
                            .statusCode(200)
                            .extract().cookie("token");
                }),
                dynamicTest("로그인된 회원 정보를 가져온다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", accessToken)
                            .when().get("/login/check")
                            .then().log().all()
                            .statusCode(200);
                }),
                dynamicTest("ADMIN 권한은 admin 페이지에 접근 가능하다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", accessToken)
                            .when().get("/admin")
                            .then().log().all()
                            .statusCode(200);
                })
        );
    }

    @DisplayName("user 권한으로 로그인 한다.")
    @TestFactory
    Stream<DynamicTest> loginWithUser() {
        return Stream.of(
                dynamicTest("로그인 하여 토큰을 발급받는다.", () -> {
                    accessToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(USER_TOKEN_REQUEST)
                            .when().post("/login")
                            .then().log().all()
                            .statusCode(200)
                            .extract().cookie("token");
                }),
                dynamicTest("로그인된 회원 정보를 가져온다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", accessToken)
                            .when().get("/login/check")
                            .then().log().all()
                            .statusCode(200);
                }),
                dynamicTest("USER 권한은 admin 페이지에 접근 할 수 없다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", accessToken)
                            .when().get("/admin")
                            .then().log().all()
                            .statusCode(401);
                })
        );
    }
}
