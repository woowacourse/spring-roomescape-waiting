package roomescape.presentation;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.business.service.ThemeService;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("새로운 테마를 생성할 때, 성공적으로 생성된 테마 정보를 반환해야 한다")
    void createTheme() {
        ThemeRequest themeRequest = new ThemeRequest("Mystery Room", "4-6 players", "썸네일");

        given()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when()
                .post("/themes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("Mystery Room"))
                .body("description", equalTo("4-6 players"));
    }

    @Test
    @DisplayName("저장된 모든 테마 정보를 조회할 때, 저장된 테마 목록을 반환해야 한다")
    void readAllThemes() {
        // Given
        ThemeRequest themeRequest1 = new ThemeRequest("Theme 1", "Description 1", "썸네일");
        given()
                .contentType(ContentType.JSON)
                .body(themeRequest1)
                .when()
                .post("/themes");

        ThemeRequest themeRequest2 = new ThemeRequest("Theme 2", "Description 2", "썸네일");
        given()
                .contentType(ContentType.JSON)
                .body(themeRequest2)
                .when()
                .post("/themes");

        when()
                .get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2));
    }

    @Test
    @DisplayName("특정 ID의 테마 데이터를 삭제할 때, 해당 테마가 성공적으로 삭제되어야 한다")
    void deleteThemeById() {
        // Given
        ThemeResponse theme = themeService.insert(new ThemeRequest("To Delete", "Some description", "썸네일90"));

        when()
                .delete("themes/{id}", theme.id())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("인기 테마 데이터를 조회할 때, 인기 테마 목록을 반환해야 한다")
    void readPopularThemes() {
        // Given
        themeService.insert(new ThemeRequest("Popular 1", "Popular description 1", "썸네일"));
        themeService.insert(new ThemeRequest("Popular 2", "Popular description 2", "썸네일120"));

        // TODO: reservation도 넣어보고 동작 확인하기!
        when()
                .get("themes/popular")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("name", hasItems("Popular 1", "Popular 2"));
    }


    @Test
    @DisplayName("필수 필드가 누락된 요청으로 테마 생성 시, 400 BAD REQUEST가 반환되어야 한다")
    void createThemeWithInvalidRequest() {
        // Given
        final ThemeRequest invalidRequest = new ThemeRequest("", "", "썸네일");

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/themes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 테마 삭제 시, 404 NOT FOUND가 반환되어야 한다")
    void deleteNonExistingTheme() {
        // Given
        final Long invalidId = 9999L;

        // When & Then
        when()
                .delete("themes/{id}", invalidId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
