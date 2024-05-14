package roomescape.controller.member;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:truncate-with-admin-and-guest.sql"})
class MemberControllerTest {
    @LocalServerPort
    private int port;
    private String adminToken;

    @BeforeEach
    void init() {
        RestAssured.port = port;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin123", "admin@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("모든 사용자 조회 성공 테스트 - 사용자 총 3명")
    @Test
    void findAllMembers() {
        //when&then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/members")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(2));
    }
}
