package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_THEME_1;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_THEME_2;

class ThemeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 테마 목록을 조회한다.")
    @Test
    void getThemes_success() {
        TypeRef<List<ThemeResponse>> ThemesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie("token", adminToken)
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
                .cookie("token", adminToken)
                .body(themeRequest)
                .when().post("/themes")
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

            CustomExceptionResponse response = sendDeleteRequest(notExistTimeId)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().as(CustomExceptionResponse.class);

            assertAll(
                    () -> assertThat(response.title()).contains("리소스를 찾을 수 없습니다."),
                    () -> assertThat(response.detail()).contains("아이디에 해당하는 테마를 찾을 수 없습니다.")
            );
        }

        @DisplayName("예외 발생 - 예약이 있는 테마를 삭제한다.") // todo: 어드민으로 변경
        @Test
        void deleteTheme_whenReservationExist_fail() {
            long themeIdWhereReservationExist = PRE_INSERTED_THEME_2.getId();

            CustomExceptionResponse response = sendDeleteRequest(themeIdWhereReservationExist)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(CustomExceptionResponse.class);

            assertAll(
                    () -> assertThat(response.title()).contains("허용되지 않는 작업입니다."),
                    () -> assertThat(response.detail()).contains("해당 테마에 예약이 존재하기 때문에 삭제할 수 없습니다.")
            );
        }

        private ValidatableResponse sendDeleteRequest(long id) {
            return RestAssured.given().log().all()
                    .when().delete("/themes/" + id)
                    .then().log().all();
        }
    }
}
