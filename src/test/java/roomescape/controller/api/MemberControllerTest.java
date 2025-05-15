package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.auth.SignUpRequestDto;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberControllerTest {

    @Nested
    class MemberRegistration {

        @DisplayName("회원가입을 할 수 있다.")
        @Test
        void registerMember() {
            SignUpRequestDto signUpRequestDto = new SignUpRequestDto("가이온", "hello@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());
        }

        @DisplayName("이미 같은 이메일로 회원가입이 되어 있으면 추가할 수 없다.")
        @Test
        void registerDuplicateMember() {
            SignUpRequestDto signUpRequestDto = new SignUpRequestDto("가이온", "hello@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(200);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    class MemberFind {
        @BeforeEach
        void setUpMember() {
            SignUpRequestDto signUpRequestDto1 = new SignUpRequestDto("가이온", "hello@woowa.com", "password");
            SignUpRequestDto signUpRequestDto2 = new SignUpRequestDto("가이온", "hello@woowa.com1", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto1)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto2)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());
        }

        @DisplayName("회원가입 된 멤버를 가져올 수 있다.")
        @Test
        void findMembers() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(2));
        }
    }
}
