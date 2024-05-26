package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.request.MemberLoginRequest;
import roomescape.controller.request.WaitingRequest;

import java.time.LocalDate;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/test_data.sql")
@Sql("/controller-test-data.sql")
class WaitingControllerTest {

    @DisplayName("예약 대기를 추가할 수 있다.")
    @Test
    void should_add_waiting_when_request() {
        MemberLoginRequest loginRequest = new MemberLoginRequest("1234", "sun@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        WaitingRequest request = new WaitingRequest(
                LocalDate.of(2030, 8, 5), 6L, 10L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie(cookie)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/waiting/1");
    }

    @DisplayName("존재하는 예약 대기라면 예약 대기를 삭제할 수 있다.")
    @Test
    void should_delete_waiting_when_waiting_exist() {
        MemberLoginRequest loginRequest = new MemberLoginRequest("1234", "sun@email.com");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");

        WaitingRequest request = new WaitingRequest(
                LocalDate.of(2030, 8, 5), 6L, 10L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie(cookie)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/waiting/1");

        RestAssured.given().log().all()
                .when().delete("/waiting/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약 대기를 조회한다.")
    @Test
    void should_get_waiting() {
        RestAssured.given().log().all()
                .when().get("/waiting")
                .then().log().all()
                .statusCode(200).extract();
    }
}
