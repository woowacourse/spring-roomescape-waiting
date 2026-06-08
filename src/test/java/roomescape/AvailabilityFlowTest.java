package roomescape;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AvailabilityFlowTest {

    @Test
    void 가용시간_조회_예약생성_재조회시_예약된_시간이_빠진다() {
        Integer themeId = createTheme("공포", "무서운 테마", "https://example.com/horror.jpg");
        Integer time10 = createTime("10:00");
        Integer time11 = createTime("11:00");
        Integer time12 = createTime("12:00");
        String date = LocalDate.now().plusDays(1).toString();

        // 1) 가용 시간 조회 - 모두 reserved=false
        RestAssured.given().log().all()
            .when().get("/times?date=" + date + "&themeId=" + themeId)
            .then().log().all()
            .statusCode(200)
            .body("size()", is(3))
            .body("find { it.id == " + time10 + " }.reserved", is(false))
            .body("find { it.id == " + time11 + " }.reserved", is(false))
            .body("find { it.id == " + time12 + " }.reserved", is(false));

        // 2) 11시로 예약 생성
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", time11);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(reservation)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201);

        // 3) 가용 시간 재조회 - 11시만 reserved=true
        RestAssured.given().log().all()
            .when().get("/times?date=" + date + "&themeId=" + themeId)
            .then().log().all()
            .statusCode(200)
            .body("size()", is(3))
            .body("find { it.id == " + time10 + " }.reserved", is(false))
            .body("find { it.id == " + time11 + " }.reserved", is(true))
            .body("find { it.id == " + time12 + " }.reserved", is(false));
    }

    private Integer createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/times")
            .then().log().all()
            .statusCode(201)
            .extract().jsonPath().get("id");
    }

    private Integer createTheme(String name, String description, String thumbnailImageUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnailImageUrl", thumbnailImageUrl);

        return RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/themes")
            .then().log().all()
            .statusCode(201)
            .extract().jsonPath().get("id");
    }
}
