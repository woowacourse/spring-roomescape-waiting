package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ThemeApiFixture {

    public static List<Map<String, String>> THEME_PARAMS_LIST = List.of(
            Map.of("name", "우가의 레이어드 아키텍처",
                    "description", "우가우가 설명",
                    "thumbnail", "따봉우가.jpg"),
            Map.of("name", "헤일러의 디버깅 교실",
                    "description", "bug입니다.",
                    "thumbnail", "좋아용.jpg"),
            Map.of("name", "테마1",
                    "description", "테마1 설명",
                    "thumbnail", "테마1.jpg"),
            Map.of("name", "테마2",
                    "description", "테마2 설명",
                    "thumbnail", "테마2.jpg"),
            Map.of("name", "테마3",
                    "description", "테마3 설명",
                    "thumbnail", "테마3.jpg"),
            Map.of("name", "테마4",
                    "description", "테마4 설명",
                    "thumbnail", "테마4.jpg"),
            Map.of("name", "테마5",
                    "description", "테마5 설명",
                    "thumbnail", "테마5.jpg")
    );

    private ThemeApiFixture() {
    }

    public static Map<String, String> themeParams1() {
        if (THEME_PARAMS_LIST.isEmpty()) {
            throw new IllegalStateException("테마 픽스처의 개수가 부족합니다.");
        }
        return THEME_PARAMS_LIST.get(0);
    }

    public static Map<String, String> themeParams2() {
        if (THEME_PARAMS_LIST.size() < 2) {
            throw new IllegalStateException("테마 픽스처의 개수가 부족합니다.");
        }
        return THEME_PARAMS_LIST.get(1);
    }

    public static List<ValidatableResponse> createThemes(
            final Map<String, String> cookies,
            final int count
    ) {
        if (THEME_PARAMS_LIST.size() < count) {
            throw new IllegalStateException("테마 픽스처의 개수가 부족합니다.");
        }

        return THEME_PARAMS_LIST.stream()
                .limit(count)
                .map(themeParams -> RestAssured.given().log().all()
                        .cookies(cookies)
                        .contentType(ContentType.JSON)
                        .body(themeParams)
                        .when().post("/themes")
                        .then().log().all()
                        .statusCode(HttpStatus.CREATED.value()))
                .toList();
    }
}
