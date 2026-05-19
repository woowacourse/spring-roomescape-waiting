package roomescape.common;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.auth.jwt.JwtProvider;
import roomescape.member.domain.Role;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"classpath:truncate.sql", "classpath:test-member.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class AcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtProvider jwtProvider;

    protected String managerToken;
    protected String memberToken;
    protected String anotherToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        managerToken = generateToken(1L, "admin", Role.MANAGER);
        memberToken = generateToken(2L, "member", Role.MEMBER);
        anotherToken = generateToken(3L, "다른사람", Role.MEMBER);
    }

    protected String generateToken(Long id, String name, Role role) {
        String token = jwtProvider.generateToken(id, name, role);
        return "Bearer " + token;
    }

}
