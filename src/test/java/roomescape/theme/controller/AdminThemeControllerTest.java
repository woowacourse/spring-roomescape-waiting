package roomescape.theme.controller;

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
public class AdminThemeControllerTest {

    @Test
    void 테마_추가_성공() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "디스커버리");
        params.put("description", "디스커버리 테마방입니다.");
        params.put("image", "http.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .and().body("name", is("디스커버리"));
    }

    @Test
    void 테마_삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/2")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약이_존재하는_테마_삭제_실패() {
        Map<String, Object> reservation = Map.of(
                "name", "로치",
                "themeId", 1L,
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 1L,
                "orderId", "order-1",
                "amount", 1000L
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(409);
    }
}
