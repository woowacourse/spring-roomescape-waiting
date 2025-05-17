package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import roomescape.theme.ui.dto.CreateThemeRequest;

public class ThemeApiFixture {

    public static final List<CreateThemeRequest> THEME_REQUESTS = List.of(
            new CreateThemeRequest("우가의 레이어드 아키텍처", "우가우가 설명", "따봉우가.jpg"),
            new CreateThemeRequest("헤일러의 디버깅 교실", "bug입니다.", "좋아용.jpg"),
            new CreateThemeRequest("테마1", "테마1 설명", "테마1.jpg"),
            new CreateThemeRequest("테마2", "테마2 설명", "테마2.jpg"),
            new CreateThemeRequest("테마3", "테마3 설명", "테마3.jpg")
    );

    private ThemeApiFixture() {
    }

    public static CreateThemeRequest themeRequest1() {
        if (THEME_REQUESTS.isEmpty()) {
            throw new IllegalStateException("테마 픽스처의 개수가 부족합니다.");
        }
        return THEME_REQUESTS.get(0);
    }

    public static CreateThemeRequest themeRequest2() {
        if (THEME_REQUESTS.size() < 2) {
            throw new IllegalStateException("테마 픽스처의 개수가 부족합니다.");
        }
        return THEME_REQUESTS.get(1);
    }

    public static List<ValidatableResponse> createThemes(
            final Map<String, String> cookies,
            final int count
    ) {
        if (THEME_REQUESTS.size() < count) {
            throw new IllegalStateException("테마 픽스처의 개수는 최대 " + THEME_REQUESTS.size() + "개만 가능합니다.");
        }

        return THEME_REQUESTS.stream()
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
