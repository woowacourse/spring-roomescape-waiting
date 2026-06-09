package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.exception.ThemeErrorCode;

@SpringWebTest
public class ThemeAdminControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("관리자가 테마를 생성한다.")
    void createTheme_success() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "우아한 테마",
                "description", "우아한테크코스 전용 테마입니다.",
                "thumbnailUrl", "https://example.com/woowa.png"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", is("우아한 테마"));
    }

    @Test
    @DisplayName("중복된 테마명으로 생성하려고 하면 409 Conflict를 반환한다.")
    void createTheme_duplicate_conflict() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "우아한 테마",
                "description", "설명",
                "thumbnailUrl", "https://example.com/woowa.png"
        );

        // First creation
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/themes")
                .then().statusCode(201);

        // Duplicate creation
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(409)
                .body("message", is(ThemeErrorCode.DUPLICATE_THEME.getMessage()));
    }

    @Test
    @DisplayName("테마 생성 시 입력 정보가 유효하지 않으면 400 Bad Request를 반환한다.")
    void createTheme_invalidInput_badRequest() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "",
                "description", "설명",
                "thumbnailUrl", "https://example.com/woowa.png"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body("message", is("테마 이름은 필수입니다."));
    }

    @Test
    @DisplayName("관리자가 테마를 삭제한다.")
    void deleteTheme_success() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "우아한 테마",
                "description", "우아한테크코스 전용 테마입니다.",
                "thumbnailUrl", "https://example.com/woowa.png"
        );

        Long id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/themes/" + id)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하려고 하면 404 Not Found를 반환한다.")
    void deleteTheme_notFound_notFound() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/themes/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", is(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("현재 예약이나 슬롯에 의해 사용 중인 테마를 삭제하려고 하면 409 Conflict를 반환한다.")
    void deleteTheme_inUse_conflict() throws Exception {
        // given
        createReservationTime("10:00");

        Map<String, Object> request = Map.of(
                "name", "우아한 테마",
                "description", "설명",
                "thumbnailUrl", "https://example.com/woowa.png"
        );
        Long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        databaseHelper.insertReservationDirectly("브라운", LocalDate.now().plusDays(1), 1L, id);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/themes/" + id)
                .then().log().all()
                .statusCode(409)
                .body("message", is(ThemeErrorCode.THEME_IN_USE.getMessage()));
    }
}
