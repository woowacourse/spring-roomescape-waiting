package roomescape.domain.view;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ControllerTest;

public class AdminViewControllerTest extends ControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpAll() {
        jdbcTemplate.update(
                "insert into member (name, email, password, role) values ('어드민', 'admin@gmail.com', '123456', 'ADMIN')");
        jdbcTemplate.update(
                "insert into member (name, email, password, role) values ('멤버', 'dodo@gmail.com', '123123', 'MEMBER')");
    }

    @DisplayName("/admin 으로 시작하는 요청 시 admin권한이 없으면 접근할 수 없다(403 Forbidden)")
    @Test
    void should_response_403_when_request_admin_prefix_with_not_having_admin_role() {
        RestAssured.given().log().all()
                .header("Cookie", getMemberCookie())
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("/admin get 요청 시 응답할 수 있다")
    @Test
    void should_response_200_when_request_admin_page() {
        RestAssured.given().log().all()
                .header("Cookie", getAdminCookie())
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/admin/reservation get 요청 시 응답할 수 있다")
    @Test
    void should_response_200_when_request_reservation_page() {
        RestAssured.given().log().all()
                .header("Cookie", getAdminCookie())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/admin/theme get 요청 시 응답할 수 있다")
    @Test
    void should_response_200_when_request_theme_page() {
        RestAssured.given().log().all()
                .header("Cookie", getAdminCookie())
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/admin/time get 요청 시 응답할 수 있다")
    @Test
    void should_response_200_when_request_time_page() {
        RestAssured.given().log().all()
                .header("Cookie", getAdminCookie())
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }
}
