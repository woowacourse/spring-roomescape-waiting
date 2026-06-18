package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class JpaNPlusOneObservationTest {

    private static final Member MEMBER = new Member("밀란");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);

        saveReservation(LocalDate.of(2026, 8, 5), LocalTime.of(10, 0), "공포");
        saveReservation(LocalDate.of(2026, 8, 6), LocalTime.of(11, 0), "추리");
        saveReservation(LocalDate.of(2026, 8, 7), LocalTime.of(12, 0), "모험");

        entityManager.flush();
        entityManager.clear();
        statistics.clear();
    }

    /*
     * 1. 시도한 코드
     *    서로 다른 시간과 테마를 가진 예약 3개를 조회하고 응답에 필요한 연관 필드에 접근한다.
     *
     * 2. 예측 SQL
     *    예약 목록 조회 후 연관 필드에 접근할 때 추가 SELECT가 발생할 것으로 예측했다.
     *
     * 3. 실제 SQL
     *    findByReserver가 반환되기 전에 예약 1번 + Theme 3번 + ReservationTime 3번으로
     *    총 7번 실행되고, 이후 연관 필드에 접근해도 SQL은 더 늘어나지 않는다.
     *
     * 4. 왜 그런가
     *    @ManyToOne 기본값이 EAGER라 조회 시점에 연관 엔티티까지 로딩하지만,
     *    EAGER는 하나의 JOIN SQL을 보장하지 않기 때문이다.
     */
    @Test
    @DisplayName("일반 목록 조회는 예약 1번과 연관 엔티티 N번씩을 추가 조회한다")
    void findByReserverCausesNPlusOne() {
        List<Reservation> reservations = reservationRepository.findByReserver(MEMBER);
        long statementCountAfterFind = statistics.getPrepareStatementCount();

        reservations.forEach(reservation -> {
            reservation.getTheme().getName();
            reservation.getTime().getStartAt();
        });

        assertThat(reservations).hasSize(3);
        assertThat(statementCountAfterFind).isEqualTo(7);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(7);
    }

    /*
     * 1. 시도한 코드
     *    동일한 예약 목록을 Theme과 ReservationTime에 대한 fetch join으로 조회한다.
     *
     * 2. 예측 SQL
     *    reservation, theme, reservation_time을 JOIN한 SELECT 1번이 실행될 것으로 예측했다.
     *
     * 3. 실제 SQL
     *    연관 필드 접근까지 포함해 SELECT가 총 1번 실행되고 예약 3개가 반환된다.
     *
     * 4. 왜 그런가
     *    fetch join이 연관 엔티티를 같은 SQL에서 영속성 컨텍스트에 적재하기 때문이다.
     *    현재 연관관계는 컬렉션이 아닌 @ManyToOne이므로 JOIN해도 예약 row 수가 증가하지 않는다.
     */
    @Test
    @DisplayName("fetch join은 예약과 연관 엔티티를 한 번의 SQL로 조회한다")
    void fetchJoinPreventsNPlusOne() {
        List<Reservation> reservations = entityManager.createQuery("""
                        SELECT reservation
                        FROM Reservation reservation
                        JOIN FETCH reservation.slot.time
                        JOIN FETCH reservation.slot.theme
                        WHERE reservation.reserver = :reserver
                        """, Reservation.class)
                .setParameter("reserver", MEMBER)
                .getResultList();
        long statementCountAfterFind = statistics.getPrepareStatementCount();

        reservations.forEach(reservation -> {
            reservation.getTheme().getName();
            reservation.getTime().getStartAt();
        });

        assertThat(reservations).hasSize(3);
        assertThat(statementCountAfterFind).isOne();
        assertThat(statistics.getPrepareStatementCount()).isOne();
    }

    /*
     * 1. 시도한 코드
     *    동일한 예약 목록 조회에 EntityGraph를 적용해
     *    Theme과 ReservationTime을 함께 조회한다.
     *
     * 2. 예측 SQL
     *    fetch join과 마찬가지로 reservation, theme,
     *    reservation_time을 JOIN한 SELECT 1번이 실행될 것으로 예측했다.
     *
     * 3. 실제 SQL
     *    연관 필드 접근까지 포함해 SELECT가 총 1번 실행되고
     *    예약 3개가 반환된다.
     *
     * 4. fetch join과 같은 점
     *    조회 대상 연관 엔티티를 같은 SQL에서 영속성 컨텍스트에 적재하므로
     *    추가 SELECT 없이 N+1 문제를 방지한다.
     *
     * 5. fetch join과 다른 점
     *    fetch join은 JPQL에 JOIN FETCH를 직접 작성하지만,
     *    EntityGraph는 조회 쿼리와 연관 엔티티의 로딩 전략을 분리한다.
     *
     *    따라서 EntityGraph는 기존 Repository 조회 메서드를 유지할 수 있지만,
     *    실제 JOIN 방식과 SQL 생성은 JPA 구현체가 결정한다.
     *    Hibernate에서는 일반적으로 EntityGraph 대상 연관관계를 LEFT JOIN으로 조회한다.
     */
    @Test
    @DisplayName("EntityGraph는 예약과 연관 엔티티를 한 번의 SQL로 조회한다")
    void entityGraphPreventsNPlusOne() {
        List<Reservation> reservations =
                reservationRepository.findAllByReserver(MEMBER);

        long statementCountAfterFind =
                statistics.getPrepareStatementCount();

        reservations.forEach(reservation -> {
            reservation.getTheme().getName();
            reservation.getTime().getStartAt();
        });

        assertThat(reservations).hasSize(3);
        assertThat(statementCountAfterFind).isOne();
        assertThat(statistics.getPrepareStatementCount()).isOne();
    }

    private void saveReservation(LocalDate date, LocalTime startAt, String themeName) {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(startAt));
        Theme theme = themeRepository.save(new Theme(
                themeName,
                themeName + " 테마",
                "https://example.com/" + themeName
        ));
        reservationRepository.save(new Reservation(MEMBER, new Slot(date, time, theme)));
    }
}
