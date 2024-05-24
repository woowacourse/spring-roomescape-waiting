package roomescape.controller.admin;

import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.ADMIN_LOGIN_REQUEST;
import static roomescape.TestFixture.MEMBER1_LOGIN_REQUEST;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.TestFixture;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminPageControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("Admin Page 홈화면 접근 성공 테스트")
    @Test
    void responseAdminPage() {
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Reservation Page 접근 성공 테스트")
    @Test
    void responseAdminReservationPage() {
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Time Page 접근 성공 테스트")
    @Test
    void responseAdminTimePage() {
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/time")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Theme Page 접근 성공 테스트")
    @Test
    void responseAdminThemePage() {
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/theme")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("관리자가 아닌 회원은 접속할 수 없다.")
    @Test
    void responseAdminPageWithoutAdmin() {
        memberRepository.save(TestFixture.MEMBER1);
        String accessToken = TestFixture.getTokenAfterLogin(MEMBER1_LOGIN_REQUEST);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
