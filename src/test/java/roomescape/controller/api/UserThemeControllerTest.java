package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;
import roomescape.service.ThemeService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserThemeControllerTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String userToken;

    @BeforeEach
    void setUpToken() {
        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('러너덕', 'user@a.com', '123a!', 'USER')");

        LoginRequest user = new LoginRequest("user@a.com", "123a!");

        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("성공: 테마 조회 -> 200")
    @Test
    void findAll() {
        themeService.save("t1", "d1", "https://test.com/test1.jpg");
        themeService.save("t2", "d2", "https://test.com/test2.jpg");

        RestAssured.given().log().all()
            .cookie("token", userToken)
            .contentType(ContentType.JSON)
            .when().get("/themes")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("name", contains("t1", "t2"))
            .body("description", contains("d1", "d2"));
    }

    @DisplayName("성공: 최근 일주일 내 상위 10개의 인기 테마 조회 -> 200")
    @Test
    void findPopular() {
        // 테마3(5회) > 테마2(4회) > 테마4(3회) > 테마5(1회) > 테마1(0회) 순서로 나오는지 검증
        // 테마1을 9일 전 예약으로 추가하여 경계값 테스트

        jdbcTemplate.update("""
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg'),
                   ('테마2', 'd2', 'https://test.com/test2.jpg'),
                   ('테마3', 'd3', 'https://test.com/test3.jpg'),
                   ('테마4', 'd4', 'https://test.com/test4.jpg'),
                   ('테마5', 'd5', 'https://test.com/test5.jpg');
                   
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00'),
                   ('09:00'),
                   ('10:00'),
                   ('11:00'),
                   ('12:00');
                   
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), '2024-01-01', 1, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), '2024-01-01', 2, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), '2024-01-01', 3, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), '2024-01-01', 4, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), '2024-01-01', 5, 3, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), '2024-01-01', 1, 2, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), '2024-01-01', 2, 2, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), '2024-01-01', 3, 2, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), '2024-01-01', 4, 2, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -7, CURRENT_DATE), '2024-01-01', 1, 4, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -7, CURRENT_DATE), '2024-01-01', 2, 4, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -7, CURRENT_DATE), '2024-01-01', 3, 4, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -8, CURRENT_DATE), '2024-01-01', 1, 5, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -8, CURRENT_DATE), '2024-01-01', 2, 5, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -9, CURRENT_DATE), '2024-01-01', 1, 1, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -9, CURRENT_DATE), '2024-01-01', 2, 1, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -9, CURRENT_DATE), '2024-01-01', 3, 1, 'RESERVED')
            """);

        RestAssured.given().log().all()
            .when().get("/themes/trending")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(3, 2, 4, 5, 1));
    }
}
