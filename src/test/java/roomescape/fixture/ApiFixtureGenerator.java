package roomescape.fixture;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;

public class ApiFixtureGenerator {

    public long createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    public long createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "description", description,
                        "thumbnail", thumbnail
                ))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    public long createReservation(String name, LocalDate date, long timeId, long themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "date", date.toString(),
                        "timeId", timeId,
                        "themeId", themeId,
                        "amount", 10000
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getLong("id");
    }

    public long createWaiting(String name, LocalDate date, long timeId, long themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "date", date.toString(),
                        "timeId", timeId,
                        "themeId", themeId
                ))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }
}
