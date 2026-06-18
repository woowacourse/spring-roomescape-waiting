package roomescape;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.theme.ThumbnailUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 영속성 컨텍스트 관찰 테스트
 *
 * 목적: 코드를 추가하기보다 JPA가 자동으로 무엇을 하는지 "관찰"한다.
 *
 * 관찰 방법:
 *   - 콘솔의 Hibernate SQL 로그를 직접 눈으로 본다 (show-sql=true 필요)
 *   - em.flush()  : 영속성 컨텍스트 → DB 동기화를 강제로 트리거
 *   - em.clear()  : 1차 캐시를 비워 다음 조회가 DB를 다시 치게 함
 *
 * 각 테스트의 핵심은 assert 통과가 아니라
 * "이 시점에 SQL이 나가는가/안 나가는가"를 로그로 확인하는 것이다.
 */

@DataJpaTest
public class PersistenceContextObservationTest {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private final static LocalDate TODAY = LocalDate.of(2026, 5, 10);

    @Autowired private ThemeRepository themeRepository;
    @Autowired private EntityManager em;


    /**
     *  관찰 1. Dirty Checking (변경 감지)
     *      - @Transactional 안에서 엔티티 필드만 수정, save() 미호출
     *      - commit/flush 시점에 UPDATE가 자동 발행되는지 본다
     */
    @Test
    @DisplayName("변경 감지: save 안 불러도 flush 시 UPDATE 발행")
    void dirtyChecking() {
        Theme theme = Theme.create(new ThemeName("테스트 테마"), "테스트 테마 입니다.", new ThumbnailUrl("https://test.com"));

        em.persist(theme);
        em.flush();
        em.clear();  // 1차 캐시 비우고 깨끗하게 다시 조회

        // when: 조회한 영속 엔티티의 필드만 수정 (save 호출 X)
        Theme found = em.find(Theme.class, theme.getId());
        found.changeDescription("수정된 설명");

        System.out.println(">>> 아직 flush 전 — UPDATE 안 나갔을 것");
        em.flush();   // ★ 이 순간 로그에 UPDATE가 찍히는지 관찰
        System.out.println(">>> flush 후 — 위에 UPDATE 로그 떴는지 확인");

        // 예측: save를 안 불렀는데도 flush 시점에 UPDATE 발행
        // 실제: 로그로 확인
    }

    /**
     * 관찰 2. 1차 캐시
     *     - 같은 트랜잭션에서 findById 두 번
     *     - 두 번째 SELECT가 생략되는지 본다
     */
    @Test
    @DisplayName("1차 캐시: 같은 트랜잭션 내 두 번째 조회는 SELECT 생략")
    void firstLevelCache() {
        Theme theme = Theme.create(new ThemeName("테스트 테마"), "테스트 테마 입니다.", new ThumbnailUrl("https://test.com"));

        em.persist(theme);
        em.flush();
        em.clear();  // 1차 캐시 비우고 깨끗하게 다시 조회

        System.out.println(">>> 첫 번째 find — SELECT 나갈 것");
        Theme first = em.find(Theme.class, theme.getId());

        System.out.println(">>> 두 번째 find — SELECT 안 나갈 것 (1차 캐시)");
        Theme second = em.find(Theme.class, theme.getId());

        // 예측: 두 번째 find는 DB를 안 친다 (캐시 적중)
        // 실제: 로그에 SELECT가 한 번만 찍히는지 확인
        assertThat(first).isSameAs(second);   // 같은 인스턴스 = 캐시에서 반환
    }

    /**
     * 관찰 3. 쓰기 지연 (write-behind)
     *     - persist 호출 후 flush 전/후 비교
     *     - INSERT가 persist 시점이 아니라 flush 시점에 나가는지 본다
     */
    @Test
    @DisplayName("쓰기 지연: INSERT는 persist가 아니라 flush 시점에 발행")
    void writeBehind() {
        testEntity testEntity = new testEntity();

        System.out.println(">>> persist 호출 — INSERT 아직 안 나갈 것");
        em.persist(testEntity);
        System.out.println(">>> persist 후 — 위에 INSERT 로그 없어야 정상");

        System.out.println(">>> flush 호출 직전");
        em.flush();   // ★ 이 순간 INSERT가 찍히는지 관찰
        System.out.println(">>> flush 후 — 위에 INSERT 로그 떴는지 확인");

        // 예측: INSERT가 persist가 아니라 flush 시점에 발행됨
        // (단, IDENTITY 전략이면 persist 시점에 바로 INSERT 나갈 수 있음 — 관찰 포인트!)
    }

    @Entity
    @SequenceGenerator(name = "seq_gen", sequenceName = "my_seq", allocationSize = 1)
    static class testEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
        private Long id;

        protected testEntity(){
        }
    }

    /***
     *  관찰 4. flush 시점 — JPQL 실행 직전 자동 flush
     *      - 수정 후 JPQL을 실행하면, 그 직전에 flush가 자동 트리거되는지 본다
     */
    @Test
    @DisplayName("flush 시점: JPQL 실행 직전 자동 flush")
    void flushBeforeQuery() {
        Theme theme = Theme.create(new ThemeName("테스트 테마"), "테스트 테마 입니다.", new ThumbnailUrl("https://test.com"));

        em.persist(theme);
        em.flush();
        em.clear();  // 1차 캐시 비우고 깨끗하게 다시 조회

        Theme found = em.find(Theme.class, theme.getId());
        found.changeDescription("변경");   // 수정만, flush 안 함

        System.out.println(">>> JPQL 실행 — 그 직전에 UPDATE가 자동으로 나갈 것");
        em.createQuery("select t from Theme t", Theme.class).getResultList();
        System.out.println(">>> 위에 UPDATE가 SELECT보다 먼저 찍혔는지 확인");

        // 예측: JPQL 실행 전 자동 flush → UPDATE가 SELECT보다 먼저
        // 이유: JPQL은 DB를 직접 조회하므로, 변경분을 먼저 반영해야 일관성 유지
    }
}
