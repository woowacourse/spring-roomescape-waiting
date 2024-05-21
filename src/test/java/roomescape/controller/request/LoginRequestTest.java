package roomescape.controller.request;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoginRequestTest {

    @DisplayName("요청된 데이터의 이메일이 null 혹은 빈 문자열인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_email_null(String email) {
        LoginRequest request = new LoginRequest(email, "password");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 이메일이 올바른 형식이 아닌 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(strings = {"email", "email#google.com", "email@"})
    void should_throw_exception_when_invalid_email(String email) {
        LoginRequest request = new LoginRequest(email, "password");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 비밀번호가 null 혹은 빈 문자열인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_password_null(String password) {
        LoginRequest request = new LoginRequest("email@google.com", password);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }
}
