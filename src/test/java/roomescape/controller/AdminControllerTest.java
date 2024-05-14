package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    private final Member member = new Member(1L, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member admin = new Member(2L, "t2@t2.com", "124", "재즈", "ADMIN");

    private String memberToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        memberToken = jwtManager.generateToken(member);
        adminToken = jwtManager.generateToken(admin);
    }

    @DisplayName("로그인하지 않은 사용자가 어드민 페이지를 요청하면 /login 페이지로 리다이렉트 시킨다.")
    @Test
    void redirect_login_page_when_not_login_member_get_admin_page() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .when().get("/admin/**")
                .then().log().all()
                .statusCode(302)
                .header("Location", "http://localhost:" + port + "/login");
    }

    @DisplayName("어드민 권한 토큰이 없는 사용자가 어드민 페이지를 요청하면 / 페이지로 리다이렉트 시킨다.")
    @Test
    void redirect_index_page_when_not_admin_get_admin_page() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("token", memberToken)
                .when().get("/admin/**")
                .then().log().all()
                .statusCode(302)
                .header("Location", "http://localhost:" + port + "/");
    }

    @DisplayName("어드민 관리 페이지 호출 테스트")
    @Test
    void admin_main_page1() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("token", adminToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 예약 관리 페이지 호출 테스트")
    @Test
    void admin_reservation_page() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("token", adminToken)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 시간 관리 페이지 호출 테스트")
    @Test
    void admin_time_page() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("token", adminToken)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민 테마 관리 페이지 호출 테스트")
    @Test
    void admin_theme_page() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("token", adminToken)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }
}
