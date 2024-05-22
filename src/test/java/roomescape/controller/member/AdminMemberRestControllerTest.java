package roomescape.controller.member;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AdminMemberRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("멤버 목록을 조회하는데 성공하면 응답과 200 상태코드를 반환한다.")
    @Test
    void return_200_when_find_all_members() {
        Member admin = new Member("t2@t2.com", "124", "재즈", "ADMIN");
        String adminToken = jwtManager.generateToken(admin);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("로그인하지 않은 유저가 멤버 목록을 조회할 시 로그인 페이지로 리다이렉트 시킨다.")
    @Test
    void return_302_when_not_login_member_find_all_members() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(302)
                .header("Location", "http://localhost:" + port + "/login");
    }

    @DisplayName("어드민이 아닌 멤버가 멤버 목록을 조회할 시 예외를 발생시키고 403 상태코드를 응답한다")
    @Test
    void return_302_when_not_admin_find_all_members() {
        Member member = new Member("t2@t2.com", "124", "재즈", "MEMBER");
        String memberToken = jwtManager.generateToken(member);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(403);
    }
}
