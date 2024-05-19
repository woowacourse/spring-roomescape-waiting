package roomescape.domain.view;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.ControllerTest;

class ClientViewControllerTest extends ControllerTest {

    @DisplayName("웰컴페이지 요청 시 응답할 수 있다(200 OK)")
    @Test
    void should_response_200_when_request_welcome_page() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }
}
