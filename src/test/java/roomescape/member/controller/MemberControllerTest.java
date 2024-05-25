package roomescape.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_ADMIN;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.MemberFixture.MEMBER_BROWN;
import static roomescape.fixture.MemberFixture.MEMBER_DUCK;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.dto.MemberResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
        // when
        List<MemberResponse> actual = RestAssured.given().log().all()
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", MemberResponse.class);

        // then
        assertThat(actual).containsExactlyInAnyOrder(
                MemberResponse.from(MEMBER_ADMIN),
                MemberResponse.from(MEMBER_BRI),
                MemberResponse.from(MEMBER_BROWN),
                MemberResponse.from(MEMBER_DUCK)
        );
    }
}
