package roomescape;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
public class FetchDefaultObservationTest {

    @Autowired
    private EntityManager em;

    // ---- 관찰 전용 엔티티: fetch를 "명시하지 않음" ----

    @Entity(name = "FetchParent")
    static class FetchParent {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        String name;

        @OneToMany(mappedBy = "parent")   // ★ fetch 미명시 → 기본 LAZY
        List<FetchChild> children = new ArrayList<>();

        FetchParent() {}
        FetchParent(String name) { this.name = name; }
    }

    @Entity(name = "FetchChild")
    static class FetchChild {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        @ManyToOne   // ★ fetch 미명시 → 기본 EAGER
        FetchParent parent;

        FetchChild() {}
        FetchChild(FetchParent parent) { this.parent = parent; }
    }

    // ================================================================
    // 관찰 A. @ManyToOne 기본값 = EAGER
    //   Child를 조회하면 parent(@ManyToOne)가 같이 딸려 나온다.
    // ================================================================
    @Test
    @DisplayName("@ManyToOne 기본 EAGER: Child 조회 시 parent까지 함께 조회됨")
    void manyToOne_isEagerByDefault() {
        FetchParent parent = new FetchParent("부모");
        em.persist(parent);
        FetchChild child = new FetchChild(parent);
        em.persist(child);
        em.flush();
        em.clear();   // 1차 캐시 비우기 (중요: 안 비우면 캐시에서 반환돼 SELECT 안 나감)

        System.out.println(">>> [ManyToOne] JPQL로 Child 조회 시작");
        FetchChild found = em.createQuery(
                        "select c from FetchChild c where c.id = :id", FetchChild.class)
                .setParameter("id", child.id)
                .getSingleResult();
        System.out.println(">>> [ManyToOne] 조회 끝 — 위에 parent SELECT까지 나갔는지 확인");
        System.out.println(">>> [ManyToOne] 이미 parent가 로딩됨 (EAGER): " + found.parent.name);

        // 예측: @ManyToOne 기본 EAGER → Child 조회 시 parent도 함께 SELECT
        // (JPQL이라 보통 별도 SELECT로 parent를 추가 조회 = N+1 형태로 드러남)
    }

    // ================================================================
    // 관찰 B. @OneToMany 기본값 = LAZY
    //   Parent를 조회해도 children은 안 나온다.
    //   children에 "실제로 접근하는 순간"에야 SELECT가 따로 나간다.
    // ================================================================
    @Test
    @DisplayName("@OneToMany 기본 LAZY: Parent 조회 시 children은 미조회, 접근 시 조회")
    void oneToMany_isLazyByDefault() {
        FetchParent parent = new FetchParent("부모");
        em.persist(parent);
        em.persist(new FetchChild(parent));
        em.persist(new FetchChild(parent));
        em.flush();
        em.clear();

        System.out.println(">>> [OneToMany] JPQL로 Parent 조회 시작");
        FetchParent found = em.createQuery(
                        "select p from FetchParent p where p.id = :id", FetchParent.class)
                .setParameter("id", parent.id)
                .getSingleResult();
        System.out.println(">>> [OneToMany] Parent 조회 끝 — children SELECT는 아직 안 나갔을 것 (LAZY)");

        System.out.println(">>> [OneToMany] 이제 children에 접근 — 이 순간 SELECT가 따로 나갈 것");
        int count = found.children.size();   // ★ 여기서 LAZY 초기화 트리거
        System.out.println(">>> [OneToMany] children 접근 후 — 위에 children SELECT가 찍혔는지 확인. size=" + count);

        // 예측: Parent 조회 시점엔 children SELECT 없음
        //       found.children.size() 호출하는 순간 children SELECT 발행
    }

    // ================================================================
    // 관찰 C. 대조 — em.find는 LAZY여도 JOIN으로 끌어올 수 있다
    //   (왜 위에서 JPQL을 썼는지 보여주는 대조 실험)
    // ================================================================
    @Test
    @DisplayName("대조: em.find는 ManyToOne을 JOIN으로 한 번에 가져옴")
    void emFind_usesJoin() {
        FetchParent parent = new FetchParent("부모");
        em.persist(parent);
        FetchChild child = new FetchChild(parent);
        em.persist(child);
        em.flush();
        em.clear();

        System.out.println(">>> [em.find] Child를 em.find로 조회 — parent까지 JOIN 한 방으로 나올 것");
        FetchChild found = em.find(FetchChild.class, child.id);
        System.out.println(">>> [em.find] 위 SELECT가 join을 포함한 단일 쿼리였는지 확인");

        // 같은 EAGER인데 JPQL(관찰 A)은 별도 SELECT, em.find는 JOIN 한 방.
        // → "EAGER = JOIN"이 아니라 "조회 방식이 SQL 전략을 결정한다"
    }
}
