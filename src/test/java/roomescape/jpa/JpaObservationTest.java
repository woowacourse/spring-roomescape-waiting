package roomescape.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.ReservationRepository;
import roomescape.repository.SessionRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@DataJpaTest
class JpaObservationTest {

    @Autowired TestEntityManager tem;
    @Autowired EntityManagerFactory emf;
    @Autowired ReservationRepository reservationRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired ThemeRepository themeRepository;
    @Autowired TimeSlotRepository timeSlotRepository;

    private Session session;

    @BeforeEach
    void setUp() {
        Theme theme = themeRepository.save(Theme.transientOf("공포", "귀신의 집", "https://url"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        session = sessionRepository.save(Session.transientOf(LocalDate.now().plusDays(1), timeSlot, theme));
    }

    // -----------------------------------------------------------------------
    // 1. 더티 체킹 (Dirty Checking)
    //    save() 호출 없이 필드만 수정 → 트랜잭션 커밋 시 UPDATE 자동 발행
    //
    //    ※ Reservation.name 은 JPA 관리 엔티티의 필드이므로, 직접 변경 가능하도록
    //      setter 없이 reflection 으로 접근하는 대신, 아래에서는 Session 엔티티의
    //      date 필드를 수정하는 방식으로 관찰합니다 (Session 에 setter 추가 불필요,
    //      EntityManager.find 로 같은 영속성 컨텍스트 안에서 수정).
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("[더티 체킹] save() 없이 필드 수정만으로 UPDATE 발행")
    void dirtyChecking() {
        // 영속성 컨텍스트가 관리하는 엔티티를 직접 find
        Session managed = tem.getEntityManager().find(Session.class, session.getId());

        // save() 호출 없이 값만 변경 (reflection 으로 private 필드 수정)
        // → flush 시점에 Hibernate 가 스냅샷과 비교 후 UPDATE 자동 발행
        try {
            var field = Session.class.getDeclaredField("date");
            field.setAccessible(true);
            field.set(managed, LocalDate.now().plusDays(99));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // flush: 영속성 컨텍스트 → DB 동기화
        // 콘솔에서 UPDATE session SET date=? WHERE id=? 확인
        tem.flush();

        // DB 에서 다시 읽어서 반영됐는지 검증
        tem.clear(); // 1차 캐시 비우기
        Session reloaded = tem.getEntityManager().find(Session.class, session.getId());
        assertThat(reloaded.getDate()).isEqualTo(LocalDate.now().plusDays(99));

        /*
         * [콘솔 출력 예시]
         *
         * update
         *     session
         * set
         *     date=?,
         *     theme_id=?,
         *     time_id=?
         * where
         *     id=?
         *
         * → save() 호출 없었지만 flush 시 UPDATE 자동 발행됨
         */
    }

    // -----------------------------------------------------------------------
    // 2. 1차 캐시
    //    같은 트랜잭션(영속성 컨텍스트) 안에서 findById 두 번 호출
    //    → 첫 번째만 SELECT, 두 번째는 캐시 적중 (SQL 미발행)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("[1차 캐시] 같은 트랜잭션에서 findById 두 번 → SELECT 1회만 발행")
    void firstLevelCache() {
        EntityManager em = tem.getEntityManager();

        System.out.println("=== 첫 번째 find: SELECT 발행 ===");
        Session first = em.find(Session.class, session.getId());

        System.out.println("=== 두 번째 find: SELECT 미발행 (캐시 적중) ===");
        Session second = em.find(Session.class, session.getId());

        // 동일 인스턴스여야 함 (캐시에서 꺼낸 동일 객체)
        assertThat(first).isSameAs(second);

        /*
         * [콘솔 출력 예시]
         *
         * === 첫 번째 find: SELECT 발행 ===
         * select s1_0.id, s1_0.date, s1_0.theme_id, s1_0.time_id
         * from session s1_0
         * where s1_0.id=?
         *
         * === 두 번째 find: SELECT 미발행 (캐시 적중) ===
         *                         ← SQL 없음. 1차 캐시에서 반환
         */
    }

    // -----------------------------------------------------------------------
    // 3. 쓰기 지연 (Write-Behind)
    //    save() 호출 시점에 INSERT 미발행 → flush/commit 시에 일괄 발행
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("[쓰기 지연] save() 직후 INSERT 미발행, flush 이후 발행")
    void writeBehind() {
        Reservation reservation = Reservation.transientOf("브라운", session);

        System.out.println("=== save() 호출 (INSERT 미발행) ===");
        Reservation saved = reservationRepository.save(reservation);

        System.out.println("=== save() 직후 id는 이미 채워짐 (IDENTITY 전략은 즉시 INSERT) ===");
        System.out.println("id = " + saved.getId());

        /*
         * [IDENTITY 전략 특이사항]
         *
         * GenerationType.IDENTITY 는 INSERT 를 실행해야 DB 가 id 를 생성하므로
         * save() 시점에 즉시 INSERT 가 발행됩니다.
         * (SEQUENCE 전략이라면 INSERT 가 쓰기 지연 버퍼에 쌓임)
         *
         * [콘솔 출력 예시]
         *
         * === save() 호출 (INSERT 미발행) ===
         * insert into reservation (name, session_id) values (?, ?)
         *                         ← IDENTITY 이므로 즉시 발행됨
         * === save() 직후 id는 이미 채워짐 ===
         * id = 1
         *
         * → SEQUENCE 전략이었다면 flush/commit 전까지 INSERT 가 지연됨
         */
        assertThat(saved.getId()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // 4. flush 시점
    //    JPQL 실행 직전에 Hibernate 가 자동 flush (FlushMode.AUTO)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("[flush 시점] JPQL 실행 직전 자동 flush → 저장 안 한 데이터가 JPQL 에 반영됨")
    void flushBeforeJpql() {
        EntityManager em = tem.getEntityManager();
        Reservation reservation = Reservation.transientOf("브라운", session);
        em.persist(reservation); // 1차 캐시에만 존재, DB 미반영 (SEQUENCE 전략이라면)

        System.out.println("=== JPQL 실행 직전 자동 flush ===");
        // JPQL 실행 전에 Hibernate 가 자동 flush 하여 DB 동기화
        Long count = em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class)
                .getSingleResult();

        System.out.println("count = " + count);
        // persist 한 데이터가 JPQL 결과에 반영됨
        assertThat(count).isGreaterThan(0);

        /*
         * [콘솔 출력 예시]
         *
         * === JPQL 실행 직전 자동 flush ===
         * insert into reservation (name, session_id) values (?, ?)   ← 자동 flush
         * select count(r1_0.id) from reservation r1_0                ← JPQL
         * count = 1
         *
         * → flush() 를 명시 호출하지 않아도 JPQL 실행 전에 자동 동기화됨
         */
    }

    // -----------------------------------------------------------------------
    // 5. fetch 기본값
    //    @ManyToOne → EAGER (기본값)
    //    @OneToMany → LAZY (기본값)
    //    현재 코드의 @ManyToOne(fetch = FetchType.LAZY) 는 명시적 오버라이드
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("[fetch 기본값] @ManyToOne 명시적 LAZY → Session 로딩 시 TimeSlot JOIN 없음")
    void fetchType() {
        tem.flush();
        tem.clear(); // 1차 캐시 비우기

        System.out.println("=== Session 로드 (TimeSlot 은 LAZY 프록시) ===");
        Session loaded = sessionRepository.findById(session.getId()).orElseThrow();

        System.out.println("=== getTimeSlot() 은 프록시 객체, 아직 SELECT 미발행 ===");
        System.out.println("=== getStartAt() 접근 시점에 SELECT 발행 ===");
        LocalTime startAt = loaded.getTimeSlot().getStartAt();
        System.out.println("startAt = " + startAt);

        /*
         * [콘솔 출력 예시]
         *
         * === Session 로드 ===
         * select s1_0.id, s1_0.date, s1_0.theme_id, s1_0.time_id
         * from session s1_0
         * where s1_0.id=?
         *                  ← time_slot JOIN 없음 (LAZY)
         *
         * === getStartAt() 접근 시점에 SELECT 발행 ===
         * select t1_0.id, t1_0.start_at
         * from time_slot t1_0
         * where t1_0.id=?
         *                  ← 프록시 초기화 시점에 별도 SELECT 발행
         */
        assertThat(startAt).isNotNull();
    }

    // -----------------------------------------------------------------------
    // 6. LazyInitializationException
    //    트랜잭션(영속성 컨텍스트) 밖에서 LAZY 필드 접근
    //    → 프록시 초기화 불가 → 예외 발생
    //
    //    @DataJpaTest 는 테스트 전체를 하나의 트랜잭션으로 감싸므로
    //    TransactionTemplate 으로 경계를 직접 제어해서 트랜잭션 종료 시뮬레이션
    // -----------------------------------------------------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("[LazyInitializationException] 트랜잭션 밖에서 LAZY 필드 접근 시 예외")
    void lazyInitializationException() {
        // em1: 데이터 저장 전용 (커밋 후 닫기)
        Long targetId;
        EntityManager em1 = emf.createEntityManager();
        try {
            em1.getTransaction().begin();
            Theme t = Theme.transientOf("공포", "설명", "url");
            em1.persist(t);
            TimeSlot ts = TimeSlot.transientOf(LocalTime.of(22, 0));
            em1.persist(ts);
            Session s = Session.transientOf(LocalDate.now().plusDays(5), ts, t);
            em1.persist(s);
            em1.getTransaction().commit();
            targetId = s.getId();
        } finally {
            em1.close();
        }

        // em2: 조회 전용 (TimeSlot 은 1차 캐시에 없으므로 LAZY 프록시로 로드)
        Session loaded;
        EntityManager em2 = emf.createEntityManager();
        try {
            em2.getTransaction().begin();
            loaded = em2.find(Session.class, targetId);
            System.out.println("em2 열려있는 동안 - Session 로드 완료, TimeSlot 은 미초기화 LAZY 프록시");
            em2.getTransaction().commit();
        } finally {
            em2.close(); // Hibernate Session 완전히 닫힘, 프록시 초기화 불가 상태
        }

        // em2 닫힘 → TimeSlot 프록시 초기화 시도 → LazyInitializationException
        System.out.println("=== em2 닫힌 후 LAZY 프록시 접근 ===");
        assertThatThrownBy(() -> loaded.getTimeSlot().getStartAt())
                .isInstanceOf(LazyInitializationException.class);

        /*
         * [콘솔 출력 예시]
         *
         * 트랜잭션 안 - 접근 가능: 1
         * === 트랜잭션 밖에서 LAZY 프록시 접근 ===
         *
         * org.hibernate.LazyInitializationException:
         *   could not initialize proxy [roomescape.domain.TimeSlot#1]
         *   - no Session
         *
         * → 트랜잭션이 끝난 순간 영속성 컨텍스트 닫힘
         *   이후 LAZY 프록시(TimeSlot)에 접근하면 초기화할 세션이 없어 예외 발생
         *   실제 서비스: @Transactional 없는 컨트롤러에서 엔티티를 직렬화할 때 동일 현상
         */
    }
}
