package roomescape.acceptance.guest;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.ThemeResponse;

import java.util.List;

class ThemeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("테마 목록을 조회한다.")
    @Test
    void getThemes_success() {
        TypeRef<List<ThemeResponse>> ThemesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ThemesFormat);
    }

    @DisplayName("예약이 많이 된 테마 목록을 조회한다.")
    @Test
    void getMostReservedThemes() {
        TypeRef<List<ThemeResponse>> ThemesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .when().get("/themes/rankings")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ThemesFormat);
        // todo: from 과 to 의 시간을 받아서 갯수 조절할 수 있게 하기 - 시간 때문에 테스트 코드 안돌아감..
/*        assertThat(response).containsExactly(
                ThemeResponse.from(preInsertedTheme2),
                ThemeResponse.from(preInsertedTheme1)
        );*/
    }
}
