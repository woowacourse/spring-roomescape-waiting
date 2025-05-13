package roomescape.member;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql("/test-schema.sql")
public class MemberApiTest {

    @DisplayName("회원 가입 API 테스트")
    @Nested
    class SignupTest {

        private final Map<String, String> validRequest = new HashMap<>(Map.of("name", "노랑",
                "email", "aaa@gmail.com",
                "password", "1234"));

        @DisplayName("회원가입을 성공할 경우 201을 반환한다.")
        @Test
        void testSignupSuccess() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", equalTo(1))
                    .body("name", equalTo("노랑"))
                    .body("email", equalTo("aaa@gmail.com"));
        }

        @DisplayName("중복 사용자 이름일 경우 400을 반환한다.")
        @Test
        void testDuplicateName() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(201);

            Map<String, String> newRequest = new HashMap<>(Map.of("name", "노랑",
                    "email", "bbb@gmail.com",
                    "password", "1234")
            );
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(newRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("중복 이메일 경우 400을 반환한다.")
        @Test
        void testDuplicateEmail() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(201);

            Map<String, String> newRequest = new HashMap<>(Map.of("name", "빨강",
                    "email", "aaa@gmail.com",
                    "password", "1234")
            );
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(newRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("이름 요청 형식이 잘못된 경우 400을 반환한다.")
        @Test
        void testInvalidName() {
            validRequest.put("name", "abcdef");
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("이메일 요청 형식이 잘못된 경우 400을 반환한다.")
        @Test
        void testInvalidEmail() {
            validRequest.put("email", "abcdef");
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(validRequest)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("비밀번호 요청 형식이 잘못된 경우 400을 반환한다.")
        @Test
        void testInvalidPassword() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(Map.of())
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
