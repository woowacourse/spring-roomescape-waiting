package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.theme.dto.ThemeRequest;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql("/truncate-with-admin-and-guest.sql")
class ThemeAcceptanceTest extends AcceptanceTest {
    private String adminToken;

    @BeforeEach
    void init() {
        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin123", "admin@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("테마 조회 성공 테스트")
    @TestFactory
    Stream<DynamicTest> findAll() {
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        return Stream.of(
                DynamicTest.dynamicTest("테마를 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON).body(themeRequest)
                            .when().post("/admin/themes")
                            .then().extract().response().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("모든 테마를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .when().get("/themes")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );

        //when&then
    }

    @DisplayName("인기 테마 조회 성공 테스트")
    @Test
    @Sql("/insert-past-reservation.sql")
    void findPopularThemes() {
        //when&then
        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(2));
    }
}
