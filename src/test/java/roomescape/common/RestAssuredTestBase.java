package roomescape.common;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.Member;
import roomescape.integration.api.RestLoginMember;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ClockConfig.class)
public class RestAssuredTestBase {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected MemberDbFixture memberDbFixture;

    @LocalServerPort
    int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @BeforeEach
    void truncateBefore() {
        DBInitializer.truncate(jdbcTemplate);
    }

    @AfterEach
    void truncateAfter() {
        DBInitializer.truncate(jdbcTemplate);
    }

    public RestLoginMember generateLoginMember(Member member) {
        Map<String, Object> request = Map.of(
                "password", "gustn111!!",
                "email", member.getEmail().email()
        );
        String sessionId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");
        return new RestLoginMember(member, sessionId);
    }
}
