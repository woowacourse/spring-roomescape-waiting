package roomescape.global.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.member.model.Member;
import roomescape.member.model.Role;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminPageControllerTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminAccessToken;

    @BeforeEach
    void initialize() {
        Member adminMember = new Member(1L, "admin", "admin@admin.com", "password", Role.ADMIN);
        adminAccessToken = jwtTokenProvider.createToken(adminMember);
    }

    @Test
    @DisplayName("/admin 요청시 메인 페이지 응답")
    void mainPage() {
        RestAssured.given().log().all()
            .cookie("token", adminAccessToken)
            .when().get("/admin")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/admin/reservation 요청시 예약 관리 페이지 응답")
    void reservationPage() {
        RestAssured.given().log().all()
            .cookie("token", adminAccessToken)
            .when().get("/admin/reservation")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("/admin/time 요청시 시간 관리 페이지 응답")
    void timePage() {
        RestAssured.given().log().all()
            .cookie("token", adminAccessToken)
            .when().get("/admin/time")
            .then().log().all()
            .statusCode(200);
    }
}
