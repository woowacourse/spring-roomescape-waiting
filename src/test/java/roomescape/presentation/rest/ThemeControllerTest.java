package roomescape.presentation.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    private static final Map<String, String> RESERVATION_BODY = Map.of(
            "name", "공포 테마",
            "description", "공포 테마 입니다",
            "thumbnail", "url"
    );

    @DisplayName("방 테마 추가 요청시, id를 포함한 방 테마와 CREATED를 응답한다")
    @Test
    void addTheme() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(RESERVATION_BODY)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", Matchers.equalTo(6))
                .body("name", Matchers.equalTo("공포 테마"));
    }

    @DisplayName("방 테마 조회 요청시, 존재하는 모든 방 테마와 OK를 응답한다")
    @Test
    void findAllTheme() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.is(5));
    }

    // TODO: 기능 삭제함 구현 필요
    @DisplayName("방 테마 삭제 요청시, 주어진 아이디에 해당하는 방 테마가 없다면 NOT FOUND를 응답한다.")
    @Test
    void removeTheme_WhenThemeDoesNotExisted() {
        RestAssured.given().log().all()
                .when().delete("/themes/1000")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("방 테마 삭제 요청시, 주어진 아이디에 해당하는 방테마가 사용중이라면 CONFLICT를 응답한다.")
    @Test
    void removeTheme() {
        RestAssured.given().log().all()
                .when().delete("/themes/3")
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value());
    }
}
