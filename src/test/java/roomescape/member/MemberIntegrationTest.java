package roomescape.member;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.testutil.IntegrationTest;

@IntegrationTest
class MemberIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("회원 목록을 조회한다.")
    void getReservationTimes() {
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '비밥', 'ADMIN', 'admin@naver.com', 'hihi')");
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'user@naver.com', 'hihi')");


        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()

                .statusCode(200)
                .body("id", hasItems(1, 2))
                .body("size()", equalTo(2));
    }
}
