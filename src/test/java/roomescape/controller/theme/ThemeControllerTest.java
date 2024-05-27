package roomescape.controller.theme;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeControllerTest {

    @LocalServerPort
    int port;

    String memberToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        memberToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("jinwuo0925@gmail.com", "1111"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("테마 조회")
    void getThemes() {
        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    @Test
    @DisplayName("인기 테마 조회")
    void getPopularThemes() {
        final LocalDate now = LocalDate.now();
        final String from = now.minusDays(8).format(DateTimeFormatter.ISO_DATE);
        final String until = now.minusDays(1).format(DateTimeFormatter.ISO_DATE);

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/themes/popular?from=" + from + "&until=" + until + "&limit=10")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }
}
