package roomescape.controller.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminWaitingControllerTest {

    @Autowired
    ReservationController reservationController;

    @LocalServerPort
    int port;

    String adminToken;
    String memberToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        adminToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("redddy@gmail.com", "0000"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

        memberToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("jinwuo0925@gmail.com", "1111"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("모든 예약 대기를 반환한다.")
    void getWaitings() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("어드민이 아니면 예약 대기 목록을 조회할 수 없다.")
    void getWaitingsFailIfNotAdmin() {
        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(401);
    }
}
