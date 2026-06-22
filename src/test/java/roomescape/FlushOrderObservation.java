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
 * flush 순서 관찰 테스트
 *
 * 목적: Hibernate가 변경을 flush할 때 "호출 순서"가 아니라
 *       "작업 타입별 정해진 순서"(INSERT → UPDATE → DELETE)로 내보냄을 관찰한다.
 *
 * 관찰 방법: System.out 마킹과 Hibernate SQL 로그 순서를 대조.
 *   remove()를 persist()보다 먼저 호출해도, 로그엔 INSERT가 DELETE보다 먼저 찍힌다.
 *
 * 주의: 실제 도메인(status 방식)은 자동 승인이 UPDATE 하나뿐이라
 *       이 현상이 발생하지 않는다. 그래서 관찰 전용 임시 엔티티로 재현한다.
 */

@DataJpaTest
public class FlushOrderObservation {

    @Autowired
    private EntityManager em;

    @Entity(name = "FlushItem")
    @SequenceGenerator(name = "flush_seq", sequenceName = "flush_seq", allocationSize = 1)
    static class FlushItem {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flush_seq")
        Long id;
        String name;

        FlushItem() {
        }

        FlushItem(String name) {
            this.name = name;
        }
    }

    // ================================================================
    // 관찰: remove를 먼저 호출해도 INSERT가 DELETE보다 먼저 flush
    // ================================================================
    @Test
    @DisplayName("flush 순서: 호출 순서와 무관하게 INSERT가 DELETE보다 먼저")
    void insertBeforeDelete() {
        // given: 미리 하나 저장, 컨텍스트 비우기
        FlushItem existing = new FlushItem("기존");
        em.persist(existing);
        em.flush();
        em.clear();

        FlushItem toDelete = em.find(FlushItem.class, existing.id);

        // when: remove를 persist보다 "먼저" 호출
        System.out.println(">>> remove 먼저 호출 (DELETE 큐에)");
        em.remove(toDelete);

        System.out.println(">>> persist 나중 호출 (SEQUENCE라 INSERT 지연됨 — 시퀀스 조회만)");
        em.persist(new FlushItem("신규"));

        System.out.println(">>> flush — 로그에 INSERT가 DELETE보다 먼저 찍히는지 확인");
        em.flush();
        System.out.println(">>> flush 끝");

        // 예측: 호출은 remove → persist 순이었지만
        //       flush 로그는 INSERT(신규) → DELETE(기존) 순으로 나감
        // 이유: Hibernate는 타입별 정해진 순서(INSERT→UPDATE→DELETE)로 flush
        //
        // ※ 만약 IDENTITY였다면: persist 때 INSERT가 이미 나가서
        //    flush 땐 DELETE만 보임 → 이 재배열 현상을 관찰 못 함
    }

    // ================================================================
    // 순서를 강제로 바꾸려면 — 중간 flush
    // ================================================================
    @Test
    @DisplayName("순서 강제: 중간에 flush()를 끼우면 DELETE를 먼저 내보낼 수 있다")
    void forceDeleteFirst() {
        FlushItem existing = new FlushItem("기존");
        em.persist(existing);
        em.flush();
        em.clear();

        FlushItem toDelete = em.find(FlushItem.class, existing.id);

        System.out.println(">>> remove 호출");
        em.remove(toDelete);

        System.out.println(">>> 중간 flush — 여기서 DELETE를 강제로 먼저 내보냄");
        em.flush();   // remove까지의 변경을 먼저 내보냄 → DELETE 발행

        System.out.println(">>> persist 호출");
        em.persist(new FlushItem("신규"));

        System.out.println(">>> 최종 flush — 이제 INSERT 발행");
        em.flush();
        System.out.println(">>> 끝");

        // 예측: 중간 flush 덕에 DELETE → INSERT 순으로 나감
        // 활용: 유니크 제약 충돌을 피하려 "지우고 만들기" 순서가 필요할 때
    }
}
