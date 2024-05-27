package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.BaseControllerTest;
import roomescape.domain.Theme;
import roomescape.service.dto.request.ThemeCreateRequest;

class AdminThemeControllerTest extends BaseControllerTest {

    @DisplayName("테마를 추가한다.")
    @Test
    void createRoomTheme() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ThemeCreateRequest("themeName", "themeDesc", "thumbnail"))
                .when().post("/admin/themes")
                .then().log().all().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteRoomTheme() {
        // given
        Theme saved = themeRepository.save(new Theme("themeName", "themeDesc", "thumbnail"));

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().delete("/admin/themes/" + saved.getId())
                .then().log().all().statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(themeRepository.count()).isEqualTo(0);
    }
}
