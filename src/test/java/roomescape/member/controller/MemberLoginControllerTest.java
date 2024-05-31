package roomescape.member.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberLoginControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    private final MemberLoginRequest request = new MemberLoginRequest("kyummi@naver.com", "1111");
    private Member member;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        member = new Member("켬미", "kyummi@naver.com", "1111");
        memberRepository.save(member);
    }

    @Test
    @DisplayName("성공 : 로그인을 할 수 있다.")
    void login() {
        String accessToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then()
                .statusCode(200)
                .log().all().extract()
                .cookie("token");

        assertThat(accessToken).isNotNull();
    }

    @Test
    @DisplayName("성공 : 로그인 정보를 확인 수 있다.")
    void loginCheck() {
        String accessToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then()
                .statusCode(200)
                .log().all().extract()
                .cookie("token");

        String actual = RestAssured.given()
                .cookie("token", accessToken)
                .when().get("/login/check")
                .then()
                .statusCode(200).extract()
                .jsonPath().getJsonObject("name");

        assertThat(actual).isEqualTo(member.getName());
    }
}
