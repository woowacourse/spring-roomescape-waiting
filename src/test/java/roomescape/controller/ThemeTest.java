package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.ThemeResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeTest {
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("theme 목록 조회 요청이 올바르게 동작한다.")
    @Test
    void given_when_GetThemes_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("랭크를 조회하면 현재 날짜 기준 일주일 동안의 인기 테마를 확인할 수 있다.")
    @Test
    void given_startDateEndDateCount_when_rank_then_statusCodeIsOk() {
        //given
        String startDate = "2024-04-30";
        String endDate = "2024-05-02";
        String size = "10";
        final List<ThemeResponse> themeResponses = RestAssured.given().log().all()
                .when().get("/themes/rank?startDate={startDate}&endDate={endDate}&size={count}", startDate, endDate, size)
                .then().extract().body()
                .jsonPath().getList("data", ThemeResponse.class);
        ThemeResponse actual = themeResponses.get(0);
        assertThat(actual.id()).isEqualTo(2L);
    }

    @DisplayName("랭크 조회 시 적절하지 않는 기간을 입력할 경우 400 오류를 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {" ", "", "2999-45-43"})
    void given_invalidStartDate_when_rank_then_statusCodeIsBadRequest(String invalidStartDate) {
        //given
        String endDate = "2024-05-02";
        String count = "10";
        RestAssured.given().log().all()
                .when().get("/themes/rank?startDate={startDate}&endDate={endDate}&size={count}", invalidStartDate, endDate, count)
                .then().log().all()
                .statusCode(400)
                .body(containsString("startDate"));
    }
}
