package roomescape;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseControllerTest {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationTimeRepository timeRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    private TestCleaner cleaner;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        cleaner.cleanAll();
    }

    protected String getMember1WithToken() {
        memberRepository.save(TestFixture.MEMBER1);
        return TestFixture.getTokenAfterLogin(TestFixture.MEMBER1_LOGIN_REQUEST);
    }

    protected String getMember2WithToken() {
        memberRepository.save(TestFixture.MEMBER2);
        return TestFixture.getTokenAfterLogin(TestFixture.MEMBER2_LOGIN_REQUEST);
    }

    protected String getAdminWithToken() {
        memberRepository.save(TestFixture.ADMIN);
        return TestFixture.getTokenAfterLogin(TestFixture.ADMIN_LOGIN_REQUEST);
    }
}
