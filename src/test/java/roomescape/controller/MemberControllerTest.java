package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.BaseControllerTest;

class MemberControllerTest extends BaseControllerTest {

    @DisplayName("모든 멤버를 조회한다.")
    @Test
    void findAll() {
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(1));
    }
}
