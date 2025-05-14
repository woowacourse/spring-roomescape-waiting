package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.repository.MemberRepository;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminPageControllerTest {

    @Autowired
    MemberRepository memberRepository;
    String adminToken;
    String userToken;

    @BeforeEach
    void setUp() {
        memberRepository.save(new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password"));
        memberRepository.save(new Member(2L, "가이온1", "hello1@woowa.com", Role.ADMIN, "password"));

        LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");
        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequestDto)
                .when().post("/login")
                .getCookies();

        userToken = cookies.get("token");

        LoginRequestDto loginRequestDto1 = new LoginRequestDto("hello1@woowa.com", "password");
        Map<String, String> cookies2 = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequestDto1)
                .when().post("/login")
                .getCookies();
        adminToken = cookies2.get("token");
    }

    @DisplayName("토큰이 없는 경우 테스트")
    @Test
    void welcomeInvalidPageTest() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("유저 토큰인 경우 테스트")
    @Test
    void reservationPageTest() {
        RestAssured.given().cookie("token", userToken).log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("Time Page 유저토큰인 경우 테스트")
    @Test
    void reservationTimePageTest() {
        RestAssured.given().cookie("token", userToken).log().all()
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("admin 토큰 테스트")
    @Test
    void welcomePageTest() {
        RestAssured.given().cookie("token", adminToken).log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }
}
