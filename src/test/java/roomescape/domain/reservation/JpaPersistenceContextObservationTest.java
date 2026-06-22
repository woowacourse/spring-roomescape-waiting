package roomescape.domain.reservation;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaPersistenceContextObservationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
    }

    @Test
    @DisplayName("dirty checking: save 없이 엔티티 필드 변경만으로 UPDATE가 발생하는지 관찰한다.")
    void observeDirtyChecking() {
        Long reservationId = createReservation("dirty-checking", LocalDate.of(2026, 6, 20), LocalTime.of(16, 0));

        tx.executeWithoutResult(status -> {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
            ReservationDate newDate = reservationDateRepository.save(
                    ReservationDate.createWithoutId(LocalDate.of(2026, 6, 21))
            );
            ReservationTime newTime = reservationTimeRepository.save(
                    ReservationTime.createWithoutId(LocalTime.of(17, 0))
            );

            // 관찰 포인트:
            // - 아래 코드는 reservationRepository.save(reservation)를 호출하지 않는다.
            // - 그런데 flush 시점에 reservation의 date_id, time_id 변경을 감지해 UPDATE가 발생하는지 콘솔을 본다.
            reservation.changeSlot(newDate, newTime);

            System.out.println("\n[dirty checking] flush 직전에 UPDATE SQL이 아직 보이지 않는지 확인");
            reservationRepository.flush();
            System.out.println("[dirty checking] flush 직후 reservation UPDATE SQL이 찍혔는지 확인\n");
        });
    }

    @Test
    @DisplayName("1차 캐시: 같은 트랜잭션에서 같은 엔티티를 두 번 조회하면 SELECT가 한 번만 발생하는지 관찰한다.")
    void observeFirstLevelCache() {
        Long reservationId = createReservation("first-cache", LocalDate.of(2026, 6, 20), LocalTime.of(16, 0));

        tx.executeWithoutResult(status -> {
            System.out.println("\n[first-level cache] 첫 번째 findById: reservation SELECT가 찍혀야 한다.");
            Reservation first = reservationRepository.findById(reservationId).orElseThrow();

            System.out.println("[first-level cache] 두 번째 findById: 같은 트랜잭션 1차 캐시 때문에 SELECT가 생략되는지 본다.");
            Reservation second = reservationRepository.findById(reservationId).orElseThrow();

            System.out.println("[first-level cache] 같은 Java 객체 참조인가? " + (first == second) + "\n");
        });
    }

    @Test
    @DisplayName("쓰기 지연: IDENTITY 전략에서 INSERT가 언제 발생하는지 관찰한다.")
    void observeWriteBehindWithIdentityStrategy() {
        tx.executeWithoutResult(status -> {
            ReservationDate date = reservationDateRepository.save(
                    ReservationDate.createWithoutId(LocalDate.of(2026, 6, 20))
            );
            ReservationTime time = reservationTimeRepository.save(
                    ReservationTime.createWithoutId(LocalTime.of(16, 0))
            );
            Theme theme = themeRepository.save(
                    Theme.createWithoutId("쓰기 지연", "IDENTITY 관찰", "/themes/write-behind")
            );

            System.out.println("\n[write-behind] Reservation save 호출 직전");
            reservationRepository.save(Reservation.createWithoutId("write-behind", date, time, theme));
            System.out.println("[write-behind] Reservation save 호출 직후");

            // 관찰 포인트:
            // - 일반적인 쓰기 지연 설명은 INSERT가 flush/commit까지 지연된다고 설명한다.
            // - 하지만 현재 엔티티들은 IDENTITY 전략을 사용하므로 DB가 id를 생성해야 한다.
            // - 그래서 Hibernate가 id를 얻기 위해 save 시점에 INSERT를 바로 실행하는지 콘솔에서 확인한다.
            // - 이 차이가 "전략에 따라 쓰기 지연 관찰 결과가 달라진다"는 핵심이다.
            System.out.println("[write-behind] flush 호출 직전: INSERT가 이미 보였는지 확인");
            reservationRepository.flush();
            System.out.println("[write-behind] flush 호출 직후: 추가 INSERT가 또 찍히는지 확인\n");
        });
    }

    @Test
    @DisplayName("flush 시점: JPQL 실행 직전에 변경 내용이 DB와 동기화되는지 관찰한다.")
    void observeFlushBeforeJpql() {
        Long reservationId = createReservation("flush-before-jpql", LocalDate.of(2026, 6, 20), LocalTime.of(16, 0));

        tx.executeWithoutResult(status -> {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
            ReservationDate newDate = reservationDateRepository.save(
                    ReservationDate.createWithoutId(LocalDate.of(2026, 6, 22))
            );
            ReservationTime newTime = reservationTimeRepository.save(
                    ReservationTime.createWithoutId(LocalTime.of(18, 0))
            );

            reservation.changeSlot(newDate, newTime);

            System.out.println("\n[flush before JPQL] JPQL 실행 직전");
            entityManager.createQuery("select r from Reservation r", Reservation.class)
                    .getResultList();
            System.out.println("[flush before JPQL] JPQL 직전에 reservation UPDATE가 먼저 flush됐는지 확인\n");
        });
    }

    @Test
    @DisplayName("fetch: ManyToOne LAZY에서 연관 필드 접근 시 추가 SELECT가 발생하는지 관찰한다.")
    void observeLazyManyToOneFetch() {
        Long reservationId = createReservation("lazy-fetch", LocalDate.of(2026, 6, 20), LocalTime.of(16, 0));

        tx.executeWithoutResult(status -> {
            entityManager.clear();

            System.out.println("\n[fetch LAZY] findById: reservation SELECT만 먼저 찍히는지 본다.");
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

            System.out.println("[fetch LAZY] getTime().getStartAt(): reservation_time SELECT가 이 시점에 추가로 찍히는지 본다.");
            reservation.getTime().getStartAt();

            // 관찰 포인트:
            // - JPA의 @ManyToOne 기본값은 EAGER다.
            // - 현재 코드는 의도적으로 fetch = LAZY를 지정했다.
            // - 그래서 예약 조회 시 join으로 time을 바로 가져오지 않고, 실제 time 필드 접근 시 SELECT가 발생한다.
            System.out.println("[fetch LAZY] ManyToOne 기본값은 EAGER지만, 현재 매핑은 명시적으로 LAZY다.\n");
        });
    }

    @Test
    @DisplayName("LazyInitializationException: 트랜잭션 밖에서 LAZY 필드 접근 시 예외가 발생하는지 관찰한다.")
    void observeLazyInitializationException() {
        Long reservationId = createReservation("lazy-exception", LocalDate.of(2026, 6, 20), LocalTime.of(16, 0));

        Reservation reservationOutsideTransaction = tx.execute(status -> {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

            // 관찰 포인트:
            // - 여기서는 time 필드에 접근하지 않는다.
            // - 트랜잭션이 끝난 뒤 밖에서 접근해야 영속성 컨텍스트가 닫힌 상태를 볼 수 있다.
            return reservation;
        });

        System.out.println("\n[lazy exception] 트랜잭션 종료 후 getTime().getStartAt() 접근");
        try {
            reservationOutsideTransaction.getTime().getStartAt();
            System.out.println("[lazy exception] 예외가 발생하지 않았다면, 이미 프록시가 초기화됐거나 설정을 다시 확인해야 한다.\n");
        } catch (RuntimeException exception) {
            System.out.println("[lazy exception] 발생한 예외 타입: " + exception.getClass().getName());
            System.out.println("[lazy exception] 메시지: " + exception.getMessage() + "\n");
        }
    }

    private Long createReservation(String name, LocalDate playDay, LocalTime startAt) {
        return tx.execute(status -> {
            ReservationDate date = reservationDateRepository.save(ReservationDate.createWithoutId(playDay));
            ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));
            Theme theme = themeRepository.save(Theme.createWithoutId(name + " 테마", "설명", "/themes/" + name));
            Reservation reservation = reservationRepository.save(Reservation.createWithoutId(name, date, time, theme));
            return reservation.getId();
        });
    }
}
