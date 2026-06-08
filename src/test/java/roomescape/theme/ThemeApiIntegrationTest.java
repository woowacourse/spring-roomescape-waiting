package roomescape.theme;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.support.ControllerTestSupport;

public class ThemeApiIntegrationTest extends ControllerTestSupport {

    @MockitoBean
    private Clock clock;

    @Test
    @DisplayName("각 날짜에 존재하는 모든 테마를 조회할 수 있다.")
    void finds_themes_available_on_each_date() {
        String accessToken = loginUserToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("date", "2026-05-05")
                .when().get("/api/themes")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4))
                .body("data[0].id", is(1))
                .body("data[1].id", is(2))
                .body("data[2].id", is(3))
                .body("data[3].id", is(4));
    }

    @Test
    @DisplayName("최근 7일 예약 개수에 따른 인기 테마를 조회할 수 있다.")
    void finds_popular_themes_by_reservations_in_last_seven_days() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 7)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        RestAssured.given().log().all()
                .when().get("/api/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(3))
                .body("data[0].id", is(2))
                .body("data[1].id", is(1))
                .body("data[2].id", is(3));
    }

    @Test
    @DisplayName("매니저는 테마를 저장할 수 있다.")
    void manager_saves_theme_successfully() {
        String accessToken = loginManagerToken();
        Map<String, String> params = new HashMap<>();
        params.put("name", "무서운게 딱 좋아");
        params.put("description", "무서운 분위기의 방탈출");
        params.put("thumbnailUrl", "https://example.com/theme.jpg");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/manager/themes")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true));
    }

    @Test
    @DisplayName("매니저는 전체 테마를 조회할 수 있다.")
    void manager_finds_all_themes_successfully() {
        String accessToken = loginManagerToken();

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/manager/themes")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4))
                .body("data[0].id", is(1))
                .body("data[1].id", is(2))
                .body("data[2].id", is(3))
                .body("data[3].id", is(4));
    }

    @Test
    @DisplayName("매니저는 테마를 추가하고 삭제할 수 있다.")
    void manager_creates_and_deletes_theme_successfully() {
        String accessToken = loginManagerToken();
        Map<String, String> params = new HashMap<>();
        params.put("name", "무서운게 딱 좋아");
        params.put("description", "무서운 분위기의 방탈출");
        params.put("thumbnailUrl", "https://example.com/theme.jpg");

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/manager/themes")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(5));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when().delete("/api/manager/themes/5")
                .then().log().all()
                .statusCode(204);
    }
}
