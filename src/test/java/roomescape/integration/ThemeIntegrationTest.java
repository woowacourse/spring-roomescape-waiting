package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ThemeIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("테마 목록 조회 API")
    class FindAllTheme {
        @Test
        void 테마_목록을_조회할_수_있다() {
            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("인기 테마 목록 조회 API")
    class FindAllPopularTheme {
        @Test
        void 최근_일주일동안_예약_건수_많은_순서대로_10개_테마를_인기_테마로_조회할_수_있다() {
            jdbcTemplate.update("INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES (?, ?, ?, ?)",
                    "1999-09-18", "1", "1", "1");

            RestAssured.given().log().all()
                    .when().get("/themes/popular")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));

            List<Map<String, Object>> response = RestAssured.get("/themes/popular")
                    .as(new TypeRef<>() {
                    });
            assertThat(response.get(0)).containsEntry("id", 1);
        }
    }

    @Nested
    @DisplayName("테마 추가 API")
    class SaveTheme {
        @Test
        void 테마를_추가할_수_있다() {
            Map<String, String> params = new HashMap<>();
            params.put("name", "레벨3");
            params.put("description", "내용이다.");
            params.put("thumbnail", "https://naver.com");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/themes/2")
                    .body("id", is(2));
        }

        @Test
        void 필드가_빈_값이면_테마를_추가할_수_없다() {
            Map<String, String> params = new HashMap<>();
            params.put("name", "레벨3");
            params.put("description", null);
            params.put("thumbnail", "https://naver.com");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("테마 삭제 API")
    class DeleteTheme {
        @Test
        void 테마를_삭제할_수_있다() {
            jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", 1);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void 존재하지_않는_테마는_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/themes/13")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        void 예약이_존재하는_테마는_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createCookies())
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
