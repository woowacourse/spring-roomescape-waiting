package roomescape.acceptance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.service.security.JwtProvider;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AcceptanceFixture {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    protected JwtProvider jwtProvider;
    @Autowired
    protected ReservationRepository reservationRepository;
    @Autowired
    protected ReservationTimeRepository timeRepository;
    @Autowired
    protected ThemeRepository themeRepository;
    @Autowired
    protected MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        timeRepository.deleteAll();
        themeRepository.deleteAll();
        memberRepository.deleteAll();

        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART");
    }
}
