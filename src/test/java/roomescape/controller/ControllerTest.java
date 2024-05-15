package roomescape.controller;

import static roomescape.Fixture.VALID_ADMIN_EMAIL;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;
import static roomescape.Fixture.VALID_USER_EMAIL;

import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;
import org.springframework.transaction.annotation.Transactional;
import roomescape.infrastructure.MemberRepository;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationTimeRepository;
import roomescape.infrastructure.ThemeRepository;
import roomescape.web.auth.JwtProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlMergeMode(MergeMode.MERGE)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
abstract class ControllerTest {

    @Autowired
    protected ReservationRepository reservationRepository;
    @Autowired
    protected ReservationTimeRepository reservationTimeRepository;
    @Autowired
    protected ThemeRepository themeRepository;
    @Autowired
    protected MemberRepository memberRepository;
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
