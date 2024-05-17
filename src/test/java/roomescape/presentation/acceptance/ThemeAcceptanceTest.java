package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.ThemeRequest;
import roomescape.application.dto.ThemeResponse;
import roomescape.application.dto.TokenRequest;
import roomescape.domain.ThemeRepository;

class ThemeAcceptanceTest extends AcceptanceTest {
    @Autowired
    private ThemeRepository themeRepository;

    private String adminToken;

    @BeforeEach
    void adminTokenSetUp() {
        TokenRequest tokenRequest = new TokenRequest("admin@wooteco.com", "wootecoCrew6!");
        adminToken = RestAssured.given()
                .contentType("application/json")
                .body(tokenRequest)
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("token");
    }

    @DisplayName("테마를 추가한다.")
    @Test
    void createThemeTest() {
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
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when()
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @DisplayName("테마가 존재해도 완료된 예약이 없으면 인기 테마도 없다.")
    @Test
    void notFoundPopularThemes() {
        themeRepository.save(ThemeFixture.defaultValue());

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when()
                .then().log().all()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(0));
    }
}
