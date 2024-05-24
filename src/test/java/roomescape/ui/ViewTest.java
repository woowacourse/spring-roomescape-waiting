package roomescape.ui;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.AcceptanceTest;
import roomescape.member.domain.Member;

import static roomescape.TestFixture.MIA_EMAIL;
import static roomescape.TestFixture.MIA_NAME;

class ViewTest extends AcceptanceTest {

    @Test
    @DisplayName("어드민 메인 페이지를 조회한다.")
    void getAdminMainPage() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("어드민 예약 페이지를 조회한다.")
    void getAdminReservationPage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin.getEmail().getValue());
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("토큰 없이 어드민 예약 페이지를 조회한다.")
    void getAdminReservationPageWithoutToken() {
        RestAssured.given().log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("일반 사용자 권한으로 어드민 예약 페이지를 조회한다.")
    void getAdminReservationPageWithoutAuthority() {
        Member member = createTestMember(MIA_EMAIL, MIA_NAME);
        String token = createTestToken(member.getEmail().getValue());
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("어드민 시간 관리 페이지를 조회한다.")
    void getTimePage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin.getEmail().getValue());
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("어드민 테마 관리 페이지를 조회한다.")
    void getThemePage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin.getEmail().getValue());
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("사용자 예약 페이지를 조회한다.")
    void getReservationPage() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("사용자 메인 페이지를 조회한다.")
    void getMainPage() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("사용자 예약 목록 페이지를 조회한다.")
    void getMyReservationsPage() {
        Member admin = createTestAdmin();
        String token = createTestToken(admin.getEmail().getValue());
        Cookie cookie = new Cookie.Builder("token", token).build();

        RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/reservation-mine")
                .then().log().all()
                .statusCode(200);
    }
}
