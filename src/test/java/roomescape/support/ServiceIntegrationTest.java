package roomescape.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 서비스 통합 테스트의 공통 베이스.
 *
 * <p>스프링 컨텍스트 + 실제 H2를 띄우지만 <b>웹 서버(포트)는 띄우지 않는다</b>.
 * 서비스 계층을 직접 호출(reservationService.create(...))해서 검증하므로 HTTP가 필요 없다.
 * 인수 테스트와 베이스를 분리한 이유가 여기에 있다 — 포트가 필요 없는 테스트에 랜덤 포트를
 * 강제로 띄우지 않기 위해서다.
 *
 * <p>검증 대상: "시스템 상태(이미 저장된 데이터)에 의존하는 비즈니스 규칙"
 * (예: 중복 예약 거부, 예약 없는 슬롯 대기 거부, 자동 승격의 결과).
 * 이런 규칙은 Mock으로 흉내 내면 거짓말 위험이 커서 실제 DB로 검증한다.
 *
 * <p>컨텍스트는 모든 테스트가 공유하고(재기동 비용 제거), 격리는 매 테스트 전 DatabaseCleaner로 보장한다.
 */
@SpringBootTest
public abstract class ServiceIntegrationTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    protected ReservationTestHelper fixture;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }
}
