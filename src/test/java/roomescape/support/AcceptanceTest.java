package roomescape.support;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * 인수(E2E) 테스트의 공통 베이스.
 *
 * <p>랜덤 포트로 실제 웹 서버를 띄우고 RestAssured로 HTTP 경계 바깥에서 요청한다.
 * 검증 대상: "사용자 관점의 시나리오가 성립하는가"
 * (예: 예약 → 내 예약 조회 → 변경 → 취소, 대기 신청 → 승격).
 *
 * <p>ServiceIntegrationTest와 코드 형태는 비슷하지만 시선이 다르다.
 * 서비스 통합 테스트는 "내가 만든 계층들이 협력하는가"(개발자 시선),
 * 인수 테스트는 "사용자가 기대하는 흐름이 성립하는가"(사용자 시선)를 검증한다.
 * 그래서 인수 테스트는 가능한 한 실제 HTTP API로만 상태를 만들고 확인한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    protected ReservationTestHelper fixture;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }
}
