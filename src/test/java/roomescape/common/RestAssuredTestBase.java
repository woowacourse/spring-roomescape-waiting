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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.integration.api.RestLoginMember;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ClockConfig.class)
public class RestAssuredTestBase {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    protected MemberRepository memberRepository;

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


    public RestLoginMember generateLoginMember() {
        return generateLogin(MemberRole.MEMBER);
    }

    public RestLoginMember generateLoginAdmin() {
        return generateLogin(MemberRole.ADMIN);
    }

    private RestLoginMember generateLogin(final MemberRole memberRole) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        Member member = memberRepository.save(new Member(
                null,
                new MemberName("홍길동"),
                new MemberEmail("leehyeonsu4888@gmail.com"),
                new MemberEncodedPassword(encoder.encode("gustn111!!")),
                memberRole
        ));

        Map<String, Object> request = Map.of(
                "password", "gustn111!!",
                "email", "leehyeonsu4888@gmail.com"
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
