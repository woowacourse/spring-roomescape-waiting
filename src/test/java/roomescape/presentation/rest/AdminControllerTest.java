package roomescape.presentation.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminControllerTest {

    private static final Map<String, String> RESERVATION_BODY = Map.of(
            "date", "3000-03-17",
            "timeId", "1",
            "themeId", "1",
            "userId", "2"
    );

    private String getAdminToken() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "admin@email.com", "password", "password"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().response().getDetailedCookies().getValue("token");
    }

    @Test
    @DisplayName("어드민 페이지에서 예약 추가 요청시, id를 포함한 예약 내용과 CREATED를 응답한다")
    void addReservationTest() {
        var token = getAdminToken();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(RESERVATION_BODY)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("date", Matchers.equalTo("3000-03-17"));

    }

    @Test
    @DisplayName("어드민 페이지에서 유저 조회 요청 시, 존재하는 유저들과 OK를 응답한다.]")
    void findAllUsersTest() {
        var token = getAdminToken();

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.is(3));
    }
}
