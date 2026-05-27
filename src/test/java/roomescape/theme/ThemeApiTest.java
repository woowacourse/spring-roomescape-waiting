package roomescape.theme;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ThemeFixture;
import roomescape.support.ApiTest;
import roomescape.support.TestDataHelper;

@ApiTest
public class ThemeApiTest {

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("테마 생성 API를 테스트합니다.")
    @Test
    void create_theme() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(ThemeFixture.horrorThemeParams())
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("name", equalTo("공포 테마"))
                .body("description", equalTo("공포 테마 설명"))
                .body("thumbnailImgUrl", equalTo("http://img.url"));
    }

    @DisplayName("테마 이름이 비어 있을 시 400 응답을 반환합니다.")
    @Test
    void create_theme_with_blank_name() {
        Map<String, String> params = new HashMap<>(ThemeFixture.horrorThemeParams());
        params.put("name", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("테마 이름은 비어있을 수 없습니다."));
    }

    @DisplayName("테마 설명이 비어 있을 시 400 응답을 반환합니다.")
    @Test
    void create_theme_with_blank_description() {
        Map<String, String> params = new HashMap<>(ThemeFixture.horrorThemeParams());
        params.put("description", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("테마 설명은 비어있을 수 없습니다."));
    }

    @DisplayName("썸네일 URL이 비어 있을 시 400 응답을 반환합니다.")
    @Test
    void create_theme_with_blank_thumbnail() {
        Map<String, String> params = new HashMap<>(ThemeFixture.horrorThemeParams());
        params.put("thumbnailImgUrl", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("errorMessage", equalTo("썸네일 이미지 URL은 비어있을 수 없습니다."));
    }

    @DisplayName("중복된 테마 생성 시 409 응답을 반환합니다.")
    @Test
    void create_duplicated_theme() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ThemeFixture.horrorThemeParams())
                .when().post("/admin/themes")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(ThemeFixture.horrorThemeParams())
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(409)
                .body("errorMessage", equalTo("이름과 설명이 같은 테마가 이미 존재합니다."));
    }

    @DisplayName("테마 삭제 API를 테스트합니다.")
    @Test
    void delete_theme() {
        Integer themeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(ThemeFixture.horrorThemeParams())
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/admin/themes/{id}", themeId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("존재하지 않는 테마 삭제 시 404 응답을 반환합니다.")
    @Test
    void delete_theme_not_found() {
        RestAssured.given().log().all()
                .when().delete("/admin/themes/100")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("전체 테마 조회 API를 테스트합니다.")
    @Test
    void find_all_themes() {
        testHelper.insertTheme(ThemeFixture.themeCreateCommand(1));
        testHelper.insertTheme(ThemeFixture.themeCreateCommand(2));

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("최근 일주일 간 인기 테마 조회 API를 테스트합니다.")
    @Test
    void find_popular_themes() {
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Long theme1Id = testHelper.insertTheme(ThemeFixture.themeCreateCommand(1));
        Long theme2Id = testHelper.insertTheme(ThemeFixture.themeCreateCommand(2));
        Long theme3Id = testHelper.insertTheme(ThemeFixture.themeCreateCommand(3));
        Long theme4Id = testHelper.insertTheme(ThemeFixture.themeCreateCommand(4));

        LocalDate today = LocalDate.now();
        LocalDate oneDayAgo = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fourDaysAgo = today.minusDays(4);
        LocalDate eightDaysAgo = today.minusDays(8);

        testHelper.insertReservation(
                "테마1 예약자1",
                oneDayAgo,
                theme1Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마1 예약자2",
                oneDayAgo,
                theme1Id,
                tenTimeId
        );
        testHelper.insertReservation(
                "테마1 예약자3",
                twoDaysAgo,
                theme1Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마1 예약자4",
                threeDaysAgo,
                theme1Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마1 예약자5",
                fourDaysAgo,
                theme1Id,
                nineTimeId
        );

        testHelper.insertReservation(
                "테마2 예약자1",
                oneDayAgo,
                theme2Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마2 예약자2",
                twoDaysAgo,
                theme2Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마2 예약자3",
                threeDaysAgo,
                theme2Id,
                nineTimeId
        );

        testHelper.insertReservation(
                "테마3 예약자1",
                oneDayAgo,
                theme3Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마3 예약자2",
                twoDaysAgo,
                theme3Id,
                nineTimeId
        );

        testHelper.insertReservation(
                "테마4 예약자1",
                oneDayAgo,
                theme4Id,
                nineTimeId
        );
        testHelper.insertReservation(
                "기간 밖 예약자",
                eightDaysAgo,
                theme4Id,
                tenTimeId
        );

        RestAssured.given().log().all()
                .when().get("/themes/popular-top-10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4))
                .body("[0].id", equalTo(theme1Id.intValue()))
                .body("[0].name", equalTo("테마 1"))
                .body("[0].reservedCount", equalTo(5))
                .body("[1].id", equalTo(theme2Id.intValue()))
                .body("[1].name", equalTo("테마 2"))
                .body("[1].reservedCount", equalTo(3))
                .body("[2].id", equalTo(theme3Id.intValue()))
                .body("[2].name", equalTo("테마 3"))
                .body("[2].reservedCount", equalTo(2))
                .body("[3].id", equalTo(theme4Id.intValue()))
                .body("[3].name", equalTo("테마 4"))
                .body("[3].reservedCount", equalTo(1));
    }
}
