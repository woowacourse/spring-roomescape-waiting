package roomescape.controller;

import static roomescape.Fixture.VALID_ADMIN_EMAIL;
import static roomescape.Fixture.VALID_USER_EMAIL;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;
import roomescape.web.auth.JwtProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlMergeMode(MergeMode.MERGE)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
abstract class ControllerTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    private JwtProvider jwtProvider;
    @LocalServerPort
    private int port;

    @BeforeEach
    protected void setPort() {
        RestAssured.port = port;
    }

    protected String getUserToken() {
        return jwtProvider.createToken(VALID_USER_EMAIL.getEmail());
    }

    protected String getAdminToken() {
        return jwtProvider.createToken(VALID_ADMIN_EMAIL.getEmail());
    }
}
