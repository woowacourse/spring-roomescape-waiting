package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.repository.JpaMemberRepository;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Sql(scripts = {"/test-data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
//@Transactional
class MemberControllerTest {

    @Nested
    class MemberRegistration {

        @DisplayName("회원가입을 할 수 있다.")
        @Test
        void registerMember() {
            SignUpRequestDto signUpRequestDto = new SignUpRequestDto("가이온", "hello1@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(200);
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
                    .statusCode(400);
        }
    }

    @Nested
    class MemberFind {

        @DisplayName("회원가입 된 멤버를 가져올 수 있다.")
        @Test
        void findMembers() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/members")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }
}
