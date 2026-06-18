package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.date.fixture.ReservationDateFixture.activeOneWeekLater;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.theme.fixture.ThemeFixture.activeTheme;
import static roomescape.time.fixture.ReservationTimeFixture.activeTime15;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest(showSql = false)
class MissionStep2ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("findById(reservationId).getTime().getStartAt() sql 발행 테스트")
    void test1() {
        Reservation reservation = saveReservation("reservation");
        Long reservationId = reservation.getId();
        entityManager.flush();
        entityManager.clear();

        Reservation found = reservationRepository.findById(reservationId).get();
        found.getTime().getStartAt();
    }

    @Test
    @DisplayName("Dirty Checking: 엔티티 필드 수정 후 save 없이 flush하면 UPDATE가 발행된다")
    void dirtyChecking() {
        Reservation reservation = saveReservation("before");
        Long reservationId = reservation.getId();
        Member after = saveMember("after");
        entityManager.flush();

        ReflectionTestUtils.setField(reservation, "member", after);
        entityManager.flush();
        entityManager.clear();

        Reservation actual = reservationRepository.findById(reservationId).get();
        assertThat(actual.getMember().getId()).isEqualTo(after.getId());
    }

    @Test
    @DisplayName("1차 캐시: 같은 트랜잭션에서 같은 id를 두 번 조회하면 같은 인스턴스를 반환한다")
    void firstLevelCache() {
        Reservation reservation = saveReservation("reservation");
        Long reservationId = reservation.getId();
        entityManager.flush();
        entityManager.clear();

        Reservation first = reservationRepository.findById(reservationId).get();
        Reservation second = reservationRepository.findById(reservationId).get();

        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("쓰기 지연: 변경 감지는 flush 전까지 DB에 반영되지 않는다")
    void writeBehindForUpdate() {
        Reservation reservation = saveReservation("before");
        Member after = saveMember("write-behind");
        entityManager.flush();

        ReflectionTestUtils.setField(reservation, "member", after);

        assertThat(countReservationByMemberId(after.getId())).isZero();

        entityManager.flush();

        assertThat(countReservationByMemberId(after.getId())).isOne();
    }

    @Test
    @DisplayName("쓰기 지연 예외: IDENTITY 전략은 id 생성을 위해 save 시점에 INSERT가 발행된다")
    void identityInsertIsNotDelayedUntilFlush() {
        int beforeSave = countReservations();

        saveReservation("identity");
        int afterSave = countReservations();
        entityManager.flush();
        int afterFlush = countReservations();

        assertThat(afterSave).isEqualTo(beforeSave + 1);
        assertThat(afterFlush).isEqualTo(afterSave);
    }

    @Nested
    @DisplayName("Flush 시점")
    class FlushTimingTest {

        @Test
        @DisplayName("명시적 flush()를 호출하면 변경 사항이 DB에 동기화된다")
        void explicitFlush() {
            Reservation reservation = saveReservation("before");
            Member after = saveMember("explicit-flush");
            entityManager.flush();

            ReflectionTestUtils.setField(reservation, "member", after);
            assertThat(countReservationByMemberId(after.getId())).isZero();

            entityManager.flush();

            assertThat(countReservationByMemberId(after.getId())).isOne();
        }

        @Test
        @DisplayName("JPQL 실행 직전에 변경 사항이 flush된다")
        void flushBeforeJpql() {
            Reservation reservation = saveReservation("before");
            Member after = saveMember("jpql-flush");
            entityManager.flush();

            ReflectionTestUtils.setField(reservation, "member", after);
            assertThat(countReservationByMemberId(after.getId())).isZero();

            List<Reservation> reservations = entityManager.getEntityManager()
                .createQuery("SELECT r FROM reservation r", Reservation.class)
                .getResultList();

            assertThat(reservations).isNotEmpty();
            assertThat(countReservationByMemberId(after.getId())).isOne();
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
        @DisplayName("트랜잭션 종료 시 변경 사항이 commit 전에 flush된다")
        void flushOnTransactionCommit() {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            Long reservationId = transactionTemplate.execute(status -> {
                Reservation reservation = saveReservation("before");
                Member after = saveMember("commit-flush");
                entityManager.flush();
                ReflectionTestUtils.setField(reservation, "member", after);
                return reservation.getId();
            });

            assertThat(reservationId).isNotNull();
            assertThat(countReservationByMemberName("commit-flush")).isOne();
        }
    }

    @Test
    @DisplayName("Fetch 기본값: ManyToOne은 EAGER, OneToMany는 LAZY가 기본값이다")
    void defaultFetchTypes() throws NoSuchMethodException {
        FetchType manyToOneDefault = (FetchType) ManyToOne.class.getMethod("fetch")
            .getDefaultValue();
        FetchType oneToManyDefault = (FetchType) OneToMany.class.getMethod("fetch")
            .getDefaultValue();

        assertThat(manyToOneDefault).isEqualTo(FetchType.EAGER);
        assertThat(oneToManyDefault).isEqualTo(FetchType.LAZY);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Sql(scripts = "classpath:truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("LazyInitializationException: 트랜잭션 밖에서 LAZY 연관 필드에 접근하면 예외가 발생한다")
    void lazyInitializationException() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Long reservationId = transactionTemplate.execute(status -> saveReservation("reservation").getId());

        Reservation found = transactionTemplate.execute(status -> {
            Reservation reservation = reservationRepository.findById(reservationId).get();
            assertThat(Hibernate.isInitialized(reservation.getTime())).isFalse();
            return reservation;
        });

        assertThatThrownBy(() -> found.getTime().getStartAt())
            .isInstanceOf(LazyInitializationException.class);
    }

    private Reservation saveReservation(String name) {
        ReservationDate date = reservationDateRepository.save(activeOneWeekLater());
        ReservationTime time = reservationTimeRepository.save(activeTime15());
        Theme theme = themeRepository.save(activeTheme());

        return reservationRepository.save(reservation(saveMember(name), date, time, theme));
    }

    private Member saveMember(String name) {
        return memberRepository.findByName(name)
            .orElseGet(() -> memberRepository.save(Member.register(name, "password")));
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
    }

    private int countReservationByMemberId(Long memberId) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservation WHERE member_id = ?",
            Integer.class,
            memberId
        );
    }

    private int countReservationByMemberName(String name) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM reservation r
                JOIN member m ON m.id = r.member_id
                WHERE m.name = ?
                """,
            Integer.class,
            name
        );
    }
}
