package roomescape.ui;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.AcceptanceTest;
import roomescape.member.domain.Member;

class ViewTest extends AcceptanceTest {

    @Test
    @DisplayName("[Step1] 어드민 메인 페이지를 조회한다.")
    void getAdminMainPage() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("[Step2] 어드민 예약 페이지를 조회한다.")
    void getAdminReservationPage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin);
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("[Step2] 토큰 없이 어드민 예약 페이지를 조회한다.")
    void getAdminReservationPageWithoutToken() {
        RestAssured.given().log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("[Step2] 일반 사용자 권한으로 어드민 예약 페이지를 조회한다.")
    void getAdminReservationPageWithoutAuthority() {
        Member member = createTestMember();
        String token = createTestToken(member);
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("[Step7] 어드민 시간 관리 페이지를 조회한다.")
    void getTimePage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin);
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("[2 - Step2] 어드민 테마 관리 페이지를 조회한다.")
    void getThemePage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin);
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("[2 - Step3] 사용자 예약 페이지를 조회한다.")
    void getReservationPage() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("[2 - Step3] 사용자 메인 페이지를 조회한다.")
    void getMainPage() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }
}
