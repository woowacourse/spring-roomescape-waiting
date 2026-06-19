package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeControllerTest {

    @Test
    void 전체테마_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(11));

    }

    @Test
    void 단일테마_조회_성공() {
        RestAssured.given().log().all()
                .when().get("/themes/1")
                .then().log().all()
                .statusCode(200)
                .body("name", is("은하수"));
    }

    @Test
    void 트렌드_테마_조회_성공() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        for (long themeId = 1L; themeId <= 10L; themeId++) {
            createReservation(themeId, date);
        }

        RestAssured.given().log().all()
                .when().get("/themes/trending?startDate=" + date + "&endDate=" + date.plusDays(1) + "&limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }

    private void createReservation(Long themeId, LocalDate date) {
        Map<String, Object> params = Map.of(
                "name", "초록",
                "themeId", themeId,
                "date", date.toString(),
                "timeId", 1L,
                "orderId", "order-" + themeId + "-" + date,
                "amount", 1000L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
