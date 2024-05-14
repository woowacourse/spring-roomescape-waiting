package roomescape.member.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MemberControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("멤버 목록을 읽을 수 있다.")
    @Test
    void findMembersTest() {
        int size = RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        Integer count = getCountOfMember();

        assertThat(size).isEqualTo(count);
    }

    private Integer getCountOfMember() {
        return jdbcTemplate.queryForObject("SELECT count(1) from member", Integer.class);
    }
}
