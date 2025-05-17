package roomescape.theme.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.dto.ThemeRequestDto;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeControllerTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void restAssuredSetUp() {
        RestAssured.port = port;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM theme");
    }

    @DisplayName("theme를 생성하면, 201 응답이 도착한다.")
    @Test
    public void add() {
        ThemeRequestDto dto = new ThemeRequestDto("a", "b", "c");

        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON).body(dto)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("전체 Theme를 조회한다.")
    @Test
    public void findAll() {
        RestAssured.given().port(port).log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Theme를 삭제한다.")
    @Test
    public void deleteById() {
        Theme theme = ThemeFixture.create("tt1", "dd1", "th1");
        Theme savedTheme = themeRepository.save(theme);

        RestAssured.given().port(port).log().all()
                .when().delete("/themes/" + savedTheme.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
