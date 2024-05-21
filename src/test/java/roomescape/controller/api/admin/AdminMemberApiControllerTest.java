package roomescape.controller.api.admin;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.BaseControllerTest;
import roomescape.util.TokenGenerator;

class AdminMemberApiControllerTest extends BaseControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, `role`) VALUES ('관리자1', 'admin@wooteco.com', '1234', 'ADMIN')");
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, `role`) VALUES ('사용자1', 'user@wooteco.com', '1234', 'USER')");
    }

    @Test
    @DisplayName("유저 목록 조회 요청이 정상적으로 수행된다.")
    void selectMembers_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200)
                .body("members.size()", is(2));
    }
}
