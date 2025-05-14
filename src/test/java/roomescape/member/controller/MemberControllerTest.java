package roomescape.member.controller;

import static org.hamcrest.CoreMatchers.is;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.utils.JdbcTemplateUtils;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class MemberControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
    }

    @DisplayName("사용자가입 요청이 들어오면 사용자를 생성한다.")
    @Test
    void createMember() {
        Map<String, String> memberCreateParams = Map.of("email", "if@woowa.com", "password", "12341234", "name", "이프");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberCreateParams)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("모든 사용자 목록을 조회한다.")
    @Test
    void findAllMember() {
        Map<String, String> memberCreateParams = Map.of("email", "if@woowa.com", "password", "12341234", "name", "이프");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberCreateParams)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .when().get("/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }
}
