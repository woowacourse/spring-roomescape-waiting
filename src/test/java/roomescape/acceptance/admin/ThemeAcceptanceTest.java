package roomescape.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import static roomescape.acceptance.Fixture.PRE_INSERTED_THEME_1;
import static roomescape.acceptance.Fixture.PRE_INSERTED_THEME_2;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.exception.RoomescapeExceptionCode.CANNOT_DELETE_THEME_REFERENCED_BY_RESERVATION;
import static roomescape.exception.RoomescapeExceptionCode.THEME_NOT_FOUND;
import static roomescape.util.CookieUtil.TOKEN_NAME;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.ExceptionResponse;

class ThemeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 테마 목록을 조회한다.")
    @Test
    void getThemes_success() {
        TypeRef<List<ThemeResponse>> ThemesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie(TOKEN_NAME, adminToken)
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ThemesFormat);
    }

    @DisplayName("관리자가 테마를 추가한다.")
    @Test
    void addTheme_success() {
        ThemeRequest themeRequest = new ThemeRequest(
                "이름",
                "요약",
                "썸네일");

        RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .cookie(TOKEN_NAME, adminToken)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("location", containsString("/themes/"))
                .extract().as(ThemeResponse.class);
    }

    @DisplayName("관리자가 테마를 삭제한다.")
    @Nested
    class deleteTheme extends NestedAcceptanceTest {

        @DisplayName("정상 작동")
        @Test
        void deleteTheme_forExist_success() {
            long existThemeId = PRE_INSERTED_THEME_1.getId();

            sendDeleteRequest(existThemeId)
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("예외 발생 - 존재하지 않는 테마를 삭제한다.")
        @Test
        void deleteTheme_forNonExist_fail() {
            long notExistTimeId = 0L;

            ExceptionResponse response = sendDeleteRequest(notExistTimeId)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().as(ExceptionResponse.class);

            assertThat(response.message()).contains(THEME_NOT_FOUND.message());
        }

        @DisplayName("예외 발생 - 예약이 있는 테마를 삭제한다.")
        @Test
        void deleteTheme_whenReservationExist_fail() {
            long themeIdWhereReservationExist = PRE_INSERTED_THEME_2.getId();

            ExceptionResponse response = sendDeleteRequest(themeIdWhereReservationExist)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(ExceptionResponse.class);

            assertThat(response.message()).contains(CANNOT_DELETE_THEME_REFERENCED_BY_RESERVATION.message());
        }

        private ValidatableResponse sendDeleteRequest(long id) {
            return RestAssured.given().log().all()
                    .cookie(TOKEN_NAME, adminToken)
                    .when().delete("/admin/themes/" + id)
                    .then().log().all();
        }
    }
}
