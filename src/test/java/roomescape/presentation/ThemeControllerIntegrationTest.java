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
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.business.service.ThemeService;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/themes";
    }

    @Test
    @DisplayName("새로운 테마를 생성할 때, 성공적으로 생성된 테마 정보를 반환해야 한다")
    void createTheme() {
        ThemeRequest themeRequest = new ThemeRequest("Mystery Room", "4-6 players", "썸네일");

        given()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("name", equalTo("Mystery Room"))
                .body("description", equalTo("4-6 players"));
    }

    @Test
    @DisplayName("저장된 모든 테마 정보를 조회할 때, 저장된 테마 목록을 반환해야 한다")
    void readAllThemes() {
        // Given
        themeService.insert(new ThemeRequest("Theme 1", "Description 1", "썸네일"));
        themeService.insert(new ThemeRequest("Theme 2", "Description 2", "썸네일"));

        when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2));
        // .body("name", hasItems("Theme 1", "Theme 2"));
    }

    @Test
    @DisplayName("특정 ID의 테마 데이터를 삭제할 때, 해당 테마가 성공적으로 삭제되어야 한다")
    void deleteThemeById() {
        // Given
        ThemeResponse theme = themeService.insert(new ThemeRequest("To Delete", "Some description", "썸네일90"));

        when()
                .delete("/{id}", theme.id())
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
                .get("/popular")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("name", hasItems("Popular 1", "Popular 2"));
    }


    @Test
    @DisplayName("필수 필드가 누락된 요청으로 테마 생성 시, 400 BAD REQUEST가 반환되어야 한다")
    void createThemeWithInvalidRequest() {
        // Given: 유효하지 않은 ThemeRequest 객체 생성 (필수 필드 누락)
        final ThemeRequest invalidRequest = new ThemeRequest("", "", "썸네일");

        // When / Then: POST 요청 후 400 BAD REQUEST 응답 검증
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value()); // 예외 메시지 검증
    }

    @Test
    @DisplayName("존재하지 않는 ID로 테마 삭제 시, 404 NOT FOUND가 반환되어야 한다")
    void deleteNonExistingTheme() {
        // Given: 존재하지 않는 ID를 정의
        final Long invalidId = 9999L;

        // When / Then: DELETE 요청 후 404 NOT FOUND 응답 검증
        when()
                .delete("/{id}", invalidId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value()); // 예외 메시지 검증
    }
}
