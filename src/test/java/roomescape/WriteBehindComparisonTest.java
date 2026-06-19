package roomescape;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * 쓰기 지연(write-behind) 비교 — IDENTITY vs SEQUENCE
 *
 * 관찰 목표:
 *   같은 persist인데 id 생성 전략에 따라 INSERT 시점이 다르다.
 *   - IDENTITY : persist 시점에 즉시 INSERT (쓰기 지연 X)
 *   - SEQUENCE : persist는 id만 받고, INSERT는 flush까지 지연 (쓰기 지연 O)
 *
 * 관찰 방법:
 *   System.out 마킹과 Hibernate SQL 로그의 순서를 대조한다.
 *   "persist 후" 마킹과 "flush 후" 마킹 사이에 INSERT 로그가 어디 찍히는지 본다.
 *
 * Deep Dive:
 *   - SEQUENCE가 뭔지 -> 전체적인 ID 생성법
 */
@DataJpaTest
public class WriteBehindComparisonTest {

    @Autowired
    private EntityManager em;

    // ---- 전략 1: IDENTITY (DB auto_increment) ----
    @Entity(name = "IdentityEntity")
    static class IdentityEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;
    }

    // ---- 전략 2: SEQUENCE (DB 시퀀스) ----
    @Entity(name = "SequenceEntity")
    @SequenceGenerator(name = "seq_gen", sequenceName = "my_seq", allocationSize = 1)
    static class SequenceEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
        Long id;
    }

    // IDENTITY — persist 시점에 즉시 INSERT
    @Test
    @DisplayName("IDENTITY: persist 시점에 바로 INSERT (지연 안 됨)")
    void identity_insertsImmediately() {
        IdentityEntity e = new IdentityEntity();

        System.out.println(">>>>> [IDENTITY] persist 호출 직전");
        em.persist(e);
        System.out.println(">>>>> [IDENTITY] persist 후 — 여기 위에 INSERT 로그가 이미 떴을 것!");
        System.out.println(">>>>> [IDENTITY] persist 직후 id = " + e.id + " (벌써 채워짐)");

        System.out.println(">>>>> [IDENTITY] flush 호출 직전");
        em.flush();
        System.out.println(">>>>> [IDENTITY] flush 후 — 여기엔 INSERT 새로 안 뜸 (이미 나갔으니)");

        // 예측: INSERT가 "persist 후" 마킹 위에 찍힌다 (즉시 발행)
        // 이유: IDENTITY는 INSERT해야 id가 생겨서, 캐시에 넣으려면 즉시 INSERT 필요
    }

    // SEQUENCE — persist는 id만, INSERT는 flush까지 지연
    @Test
    @DisplayName("SEQUENCE: persist는 id만 받고 INSERT는 flush 시점")
    void sequence_defersInsert() {
        SequenceEntity e = new SequenceEntity();

        System.out.println(">>>>> [SEQUENCE] persist 호출 직전");
        em.persist(e);
        System.out.println(">>>>> [SEQUENCE] persist 후 — 여기 위엔 시퀀스 조회(call next value)만 있고 INSERT는 아직 없을 것!");
        System.out.println(">>>>> [SEQUENCE] persist 직후 id = " + e.id + " (시퀀스에서 받아 채워짐, INSERT는 아직)");

        System.out.println(">>>>> [SEQUENCE] flush 호출 직전");
        em.flush();
        System.out.println(">>>>> [SEQUENCE] flush 후 — 여기 위에 INSERT가 이제야 찍혔을 것!");

        // 예측: "persist 후"엔 시퀀스 조회만, INSERT는 "flush 후" 위에 찍힘 (지연됨)
        // 이유: SEQUENCE는 INSERT 없이 id를 미리 받을 수 있어, INSERT를 flush까지 미룸
    }

    /**
     * 결정적 비교 — 여러 개 persist 후 한 번에 flush
     *     - SEQUENCE는 INSERT 3개가 flush 때 몰려서 나가고, IDENTITY는 persist마다 INSERT가 흩어져 나간다.
     */
    @Test
    @DisplayName("다건: SEQUENCE는 flush 때 INSERT 몰림, IDENTITY는 persist마다 흩어짐")
    void batchComparison() {
        System.out.println(">>>>> [SEQUENCE 다건] persist 3번 시작");
        em.persist(new SequenceEntity());
        em.persist(new SequenceEntity());
        em.persist(new SequenceEntity());
        System.out.println(">>>>> [SEQUENCE 다건] persist 3번 끝 — INSERT 아직 안 나갔을 것 (시퀀스 조회만)");

        System.out.println(">>>>> [SEQUENCE 다건] flush — 여기서 INSERT 3개가 몰려서 나갈 것");
        em.flush();
        System.out.println(">>>>> [SEQUENCE 다건] flush 끝");
    }
}
