package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.IntegrationTestSupport;

class ReservationTimeControllerTest extends IntegrationTestSupport {

    String createdId;
    int timeSize;

    @DisplayName("예약 시간 CRUD")
    @TestFactory
    Stream<DynamicTest> dynamicUserTestsFromCollection() {
        return Stream.of(
                dynamicTest("예약 시간 목록을 조회한다.", () -> {
                    timeSize = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/times")
                            .then().log().all()
                            .statusCode(200).extract()
                            .response().jsonPath().getList("$").size();
                }),
                dynamicTest("예약 시간을 추가한다.", () -> {
                    Map<String, String> param = Map.of("startAt", "12:12");

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(param)
                            .when().post("/admin/times")
                            .then().log().all()
                            .statusCode(201).extract().header("location").split("/")[2];
                }),
                dynamicTest("예약 시간 목록 개수가 1증가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/times")
                            .then().log().all()
                            .statusCode(200).body("size()", is(timeSize + 1));
                }),
                dynamicTest("유효하지 않은 형식으로 시간을 추가할 수 없다.", () -> {
                    Map<String, String> param = Map.of("startAt", "12:12:12");

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .body(param)
                            .when().post("/admin/times")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("시간을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/times/" + createdId)
                            .then().log().all()
                            .statusCode(204);
                }),
                dynamicTest("예약 시간 목록 개수가 1감소한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().get("/times")
                            .then().log().all()
                            .statusCode(200).body("size()", is(timeSize));
                })
        );
    }
}
