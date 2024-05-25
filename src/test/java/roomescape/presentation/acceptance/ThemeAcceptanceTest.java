package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ThemeFixture;
import roomescape.application.dto.ThemeRequest;
import roomescape.application.dto.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeCommandRepository;

class ThemeAcceptanceTest extends AcceptanceTest {

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @DisplayName("테마를 추가한다.")
    @Test
    void createThemeTest() {
        adminTokenSetup();
        ThemeRequest request = new ThemeRequest("이름", "설명", "url");

        ThemeResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ThemeResponse.class);

        assertThat(response.name()).isEqualTo("이름");
        assertThat(response.description()).isEqualTo("설명");
        assertThat(response.thumbnail()).isEqualTo("url");
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void findPopularThemes() {
        adminTokenSetup();

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @DisplayName("테마가 존재해도 완료된 예약이 없으면 인기 테마도 없다.")
    @Test
    void notFoundPopularThemes() {
        adminTokenSetup();
        themeCommandRepository.save(ThemeFixture.defaultValue());

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAllThemes() {
        adminTokenSetup();

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteThemeById() {
        adminTokenSetup();
        Theme theme = themeCommandRepository.save(ThemeFixture.defaultValue());

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("themes/" + theme.getId())
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 테마를 삭제 요청하면 예외가 발생한다.")
    @Test
    void deleteThemeThrowsExceptionByNotFoundThemeId() {
        adminTokenSetup();

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("themes/100")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("삭제하려는 테마가 이미 예약되어있다면 예외가 발생한다.")
    @Test
    void deleteThemeThrowsExceptionByAlreadyReservedTheme() {
        adminTokenSetup();

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("themes/1")
                .then().log().all()
                .statusCode(409);
    }
}
