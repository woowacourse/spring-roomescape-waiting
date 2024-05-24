package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import roomescape.web.controller.request.ThemeWebRequest;


class ThemeControllerTest extends ControllerTest {

    @BeforeEach
    void setInitialData() {
        themeRepository.save(VALID_THEME);
    }

    @DisplayName("테마를 생성한다 -> 201")
    @Test
    void create() {
        ThemeWebRequest request = new ThemeWebRequest("test", "대충 설명", "https://url.jpg");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", is(2));
    }

    @DisplayName("테마를 삭제한다 -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(204);

        Long count = themeRepository.count();

        assertThat(count).isEqualTo(0);
    }

    @DisplayName("테마를 조회한다 -> 200")
    @Test
    void findAll() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("테마 정보 포맷이 잘못될 경우 -> 400")
    @Test
    void create_IllegalTheme() {
        ThemeWebRequest request = new ThemeWebRequest("방탈출3", "설명3", "ftp://url3");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("중복된 데이터를 추가한다 -> 400")
    @Test
    void create_Duplicate() {
        ThemeWebRequest request = new ThemeWebRequest(VALID_THEME.getName(), VALID_THEME.getDescription(),
                VALID_THEME.getThumbnail());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청이 잘못된 형식일 경우 -> 400")
    @Test
    void create_MethodArgNotValid() {
        ThemeWebRequest request = new ThemeWebRequest("", null, "https://url1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약이 존재한 상태에서 테마를 삭제한다 -> 400")
    @Test
    void delete_ReservationExists() {
        reservationTimeRepository.save(VALID_RESERVATION_TIME);
        memberRepository.save(VALID_MEMBER);
        reservationRepository.save(VALID_RESERVATION);

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("상위 10개 인기 테마를 조회 한다. -> 200")
    @Test
    @Sql(value = {"/popularTestData.sql"})
    void findPopularTheme() {
        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200)
                .body("[0].name", is("theme3"));
    }
}
