package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class JpaPersistenceContextObservationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Statistics statistics;
    private Long reservationId;
    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);

        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://example.com/horror.jpg"));
        Reservation reservation = reservationRepository.save(new Reservation(
                new Member("밀란"),
                new Slot(LocalDate.of(2026, 8, 5), time, theme)
        ));

        timeId = time.getId();
        themeId = theme.getId();
        reservationId = reservation.getId();

        entityManager.flush();
        entityManager.clear();
        statistics.clear();
    }

    /*
     * 1. 시도한 코드
     *    영속 상태의 Reservation.reserver를 save 없이 변경하고 flush한다.
     *
     * 2. 예측한 SQL/동작
     *    save를 호출하지 않았으므로 UPDATE가 나가지 않을 것처럼 보일 수 있다.
     *
     * 3. 실제 SQL/동작
     *    flush 시점에 reservation.name UPDATE가 발행된다.
     *
     * 4. 왜 다른가
     *    영속성 컨텍스트가 최초 조회 스냅샷과 현재 엔티티 상태를 비교해 dirty checking을 수행하기 때문이다.
     */
    @Test
    @DisplayName("dirty checking: save 없이 엔티티 변경 후 flush하면 UPDATE가 발행된다")
    void dirtyChecking() {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        statistics.clear();

        ReflectionTestUtils.setField(reservation, "reserver", new Member("브라운"));

        String beforeFlushName = findReservationName();
        entityManager.flush();
        String afterFlushName = findReservationName();

        assertThat(beforeFlushName).isEqualTo("밀란");
        assertThat(afterFlushName).isEqualTo("브라운");
        assertThat(statistics.getEntityUpdateCount()).isOne();
    }

    /*
     * 1. 시도한 코드
     *    같은 트랜잭션 안에서 findById(reservationId)를 두 번 호출한다.
     *
     * 2. 예측한 SQL/동작
     *    findById를 두 번 호출했으므로 SELECT도 두 번 나갈 것처럼 보일 수 있다.
     *
     * 3. 실제 SQL/동작
     *    첫 번째 호출만 SELECT가 나가고, 두 번째 호출은 같은 객체 인스턴스를 반환한다.
     *
     * 4. 왜 다른가
     *    첫 조회 결과가 영속성 컨텍스트의 1차 캐시에 저장되기 때문이다.
     */
    @Test
    @DisplayName("1차 캐시: 같은 트랜잭션에서 같은 id를 두 번 조회하면 SELECT는 한 번만 발생한다")
    void firstLevelCache() {
        Reservation first = reservationRepository.findById(reservationId).orElseThrow();
        Reservation second = reservationRepository.findById(reservationId).orElseThrow();

        assertThat(first).isSameAs(second);
        assertThat(statistics.getPrepareStatementCount()).isOne();
    }

    /*
     * 1. 시도한 코드
     *    새 Reservation을 save하고 flush 전 DB 상태를 확인한다.
     *
     * 2. 예측한 SQL/동작
     *    쓰기 지연 때문에 flush/commit 전까지 INSERT가 나가지 않을 것처럼 보일 수 있다.
     *
     * 3. 실제 SQL/동작
     *    save 직후 INSERT가 발행되고, flush 전에도 DB에서 row가 조회된다.
     *
     * 4. 왜 다른가
     *    현재 id 생성 전략이 IDENTITY라서 DB가 생성한 id를 받기 위해 INSERT가 즉시 필요하기 때문이다.
     */
    @Test
    @DisplayName("쓰기 지연: IDENTITY 전략에서는 save 시점에 INSERT가 먼저 발행될 수 있다")
    void writeBehindWithIdentity() {
        Slot slot = new Slot(
                LocalDate.of(2026, 8, 6),
                entityManager.getReference(ReservationTime.class, timeId),
                entityManager.getReference(Theme.class, themeId)
        );

        statistics.clear();
        Reservation saved = reservationRepository.save(new Reservation(new Member("브리"), slot));

        Integer countBeforeFlush = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE id = ?",
                Integer.class,
                saved.getId()
        );

        assertThat(countBeforeFlush).isOne();
        assertThat(statistics.getEntityInsertCount()).isOne();
    }

    /*
     * 1. 시도한 코드
     *    영속 엔티티를 변경한 뒤 명시적 flush 없이 JPQL을 실행한다.
     *
     * 2. 예측한 SQL/동작
     *    flush를 직접 호출하지 않았으므로 UPDATE 없이 SELECT만 나갈 것처럼 보일 수 있다.
     *
     * 3. 실제 SQL/동작
     *    JPQL 실행 직전에 UPDATE가 먼저 발행된다.
     *
     * 4. 왜 다른가
     *    FlushMode.AUTO에서는 JPQL 결과와 DB 상태를 맞추기 위해 쿼리 실행 전에 flush가 발생한다.
     */
    @Test
    @DisplayName("flush 시점: JPQL 실행 직전에 변경 내용이 DB와 동기화된다")
    void flushBeforeJpql() {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        statistics.clear();

        ReflectionTestUtils.setField(reservation, "reserver", new Member("네오"));
        entityManager.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class)
                .getSingleResult();
        
        assertThat(findReservationName()).isEqualTo("네오");
        assertThat(statistics.getEntityUpdateCount()).isOne();
    }

    /*
     * 1. 시도한 코드
     *    Reservation만 findById로 조회한 뒤 time/theme 로딩 여부를 확인한다.
     *
     * 2. 예측한 SQL/동작
     *    Reservation만 조회하고 time/theme은 나중에 접근할 때 조회될 것처럼 보일 수 있다.
     *
     * 3. 실제 SQL/동작
     *    @ManyToOne 기본값이 EAGER라 Reservation 조회 시 time/theme도 이미 로딩된다.
     *
     * 4. 왜 다른가
     *    JPA 기본 fetch 전략은 @ManyToOne, @OneToOne은 EAGER이고 @OneToMany, @ManyToMany는 LAZY이기 때문이다.
     */
    @Test
    @DisplayName("fetch 기본값: @ManyToOne은 기본 EAGER라 예약 조회 시 시간과 테마도 함께 로딩된다")
    void manyToOneDefaultFetchIsEager() {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(reservation.getTime())).isTrue();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(reservation.getTheme())).isTrue();
    }

    /*
     * 1. 시도한 코드
     *    getReference로 Theme 프록시만 얻고, detach 후 name에 접근한다.
     *
     * 2. 예측한 SQL/동작
     *    id를 알고 있으니 name도 조회될 것처럼 보일 수 있다.
     *
     * 3. 실제 SQL/동작
     *    프록시를 초기화할 영속성 컨텍스트가 없어 LazyInitializationException이 발생한다.
     *
     * 4. 왜 다른가
     *    지연 로딩 프록시는 실제 필드 접근 시 영속성 컨텍스트를 통해 DB 조회를 해야 하기 때문이다.
     */
    @Test
    @DisplayName("LazyInitializationException: 초기화되지 않은 프록시를 영속성 컨텍스트 밖에서 접근하면 예외가 발생한다")
    void lazyInitializationException() {
        Theme themeProxy = entityManager.getReference(Theme.class, themeId);
        entityManager.detach(themeProxy);

        assertThatThrownBy(themeProxy::getName)
                .isInstanceOf(LazyInitializationException.class);
    }

    private String findReservationName() {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE id = ?",
                String.class,
                reservationId
        );
    }
}
