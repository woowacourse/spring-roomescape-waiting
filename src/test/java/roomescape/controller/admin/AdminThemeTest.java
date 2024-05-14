package roomescape.controller.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.JwtTokenProvider;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminThemeTest {
    private static final String ADMIN_USER = "wedge@test.com";

    @LocalServerPort
    int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String generateToken(String email) {
        return jwtTokenProvider.createToken(email);
    }

    @DisplayName("테마 등록 성공 시 201을 응답한다.")
    @Test
    void given_themeRequest_when_saveSuccessful_then_statusCodeIsCreated() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "우테코 레벨 1 탈출");
        params.put("description", "우테코 레벨 1 탈출하는 내용");
        params.put("thumbnail", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마 삭제 성공 시 204를 응답한다.")
    @Test
    void given_when_deleteSuccessful_then_statusCodeIsNoContents() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "우테코 레벨 1 탈출");
        params.put("description", "우테코 레벨 1 탈출하는 내용");
        params.put("thumbnail", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .when().delete("/admin/themes/4")
                .then().log().all()
                .statusCode(204);
    }


    @DisplayName("삭제하고자 하는 테마에 예약이 등록되어 있으면 400 오류를 반환한다.")
    @Test
    void given_when_deleteThemeIdRegisteredReservation_then_statusCodeIsBadRequest() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(400)
                .body(containsString("예약이 등록된 테마는 제거할 수 없습니다"));
    }

    @DisplayName("테마 등록 시 빈 값이 한 개 이상 포함되어 있을 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @CsvSource({",test,test,name", "test,,test,description", "test,test,,thumbnail"})
    void given_when_saveThemeWithEmptyValues_then_statusCodeIsBadRequest(String name, String description, String thumbNail, String emptyFieldName) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnail", thumbNail);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString(emptyFieldName));
    }
}
