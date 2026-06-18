package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservation.jpa.ReservationJpaEntity;
import roomescape.repository.reservation.jpa.ReservationJpaRepository;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaEntity;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaRepository;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaRepository;
import roomescape.repository.theme.jpa.ThemeJpaEntity;
import roomescape.repository.theme.jpa.ThemeJpaRepository;

@SpringBootTest
class PersistenceContextObservationTest {

    private static long sequence;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    private ReservationSlotJpaRepository reservationSlotJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Test
    void dirty_checking_updates_without_save() {
        Long reservationId = createReservation("dc");
        Long nextSlotId = new TransactionTemplate(transactionManager).execute(status -> {
            ReservationJpaEntity reservation = entityManager.find(ReservationJpaEntity.class, reservationId);
            ThemeJpaEntity theme = reservation.getSlot().getTheme();
            ReservationTimeJpaEntity time = reservation.getSlot().getTime();

            ReservationSlotJpaEntity nextSlot = reservationSlotJpaRepository.saveAndFlush(
                    ReservationSlotJpaEntity.from(
                            new ReservationSlot(
                                    LocalDate.of(2026, 8, 20),
                                    theme.toDomain(),
                                    time.toDomain()
                            ),
                            theme,
                            time
                    )
            );

            reservation.setSlot(nextSlot);
            return nextSlot.getId();
        });

        Integer slotId = jdbcTemplate.queryForObject(
                "select slot_id from reservation where id = ?",
                Integer.class,
                reservationId
        );

        assertThat(slotId).isEqualTo(nextSlotId.intValue());
    }

    @Test
    void first_level_cache_hits_same_entity_in_same_transaction() {
        Long reservationId = createReservation("cc");
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        new TransactionTemplate(transactionManager).execute(status -> {
            ReservationJpaEntity first = entityManager.find(ReservationJpaEntity.class, reservationId);
            ReservationJpaEntity second = entityManager.find(ReservationJpaEntity.class, reservationId);

            assertThat(first).isSameAs(second);
            return null;
        });

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    void write_behind_persists_on_flush_not_before() {
        long current = nextSequence();
        String probeName = "write-behind-theme-" + current;

        new TransactionTemplate(transactionManager).execute(status -> {
            entityManager.persist(new WriteBehindProbeEntity(current, probeName));

            Integer beforeFlush = jdbcTemplate.queryForObject(
                    "select count(1) from write_behind_probe where name = ?",
                    Integer.class,
                    probeName
            );

            entityManager.flush();

            Integer afterFlush = jdbcTemplate.queryForObject(
                    "select count(1) from write_behind_probe where name = ?",
                    Integer.class,
                    probeName
            );

            assertThat(beforeFlush).isZero();
            assertThat(afterFlush).isOne();
            return null;
        });
    }

    @Test
    void jpql_query_triggers_flush_before_execution() {
        long current = nextSequence();
        String probeName = "jpql-flush-theme-" + current;

        new TransactionTemplate(transactionManager).execute(status -> {
            entityManager.persist(new WriteBehindProbeEntity(current, probeName));

            Long count = entityManager.createQuery(
                            "select count(t) from WriteBehindProbeEntity t where t.name = :name",
                            Long.class
                    )
                    .setParameter("name", probeName)
                    .getSingleResult();

            assertThat(count).isEqualTo(1L);
            return null;
        });
    }

    @Test
    void lazy_initialization_exception_occurs_outside_transaction() {
        Long reservationId = createReservation("lz");
        ReservationJpaEntity detachedReservation = new TransactionTemplate(transactionManager).execute(status ->
                entityManager.find(ReservationJpaEntity.class, reservationId)
        );

        assertThatThrownBy(() -> detachedReservation.getSlot().getTime().getStartAt())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void fetch_defaults_follow_jpa_annotation_defaults() throws Exception {
        Field manyToOneField = FetchDefaultProbe.class.getDeclaredField("manyToOne");
        Field oneToManyField = FetchDefaultProbe.class.getDeclaredField("oneToMany");

        assertThat(manyToOneField.getAnnotation(ManyToOne.class).fetch())
                .isEqualTo(jakarta.persistence.FetchType.EAGER);
        assertThat(oneToManyField.getAnnotation(OneToMany.class).fetch())
                .isEqualTo(jakarta.persistence.FetchType.LAZY);
    }

    private Long createReservation(final String reservationNamePrefix) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            long current = nextSequence();
            String reservationName = reservationNamePrefix + "-" + current;
            LocalDate date = LocalDate.of(2027, 8, 19).plusDays(current);

            ThemeJpaEntity theme = themeJpaRepository.findAll().get(0);
            ReservationTimeJpaEntity time = reservationTimeJpaRepository.findAll().get(0);
            ReservationSlotJpaEntity slot = reservationSlotJpaRepository.saveAndFlush(
                    ReservationSlotJpaEntity.from(
                            new ReservationSlot(
                                    date,
                                    theme.toDomain(),
                                    time.toDomain()
                            ),
                            theme,
                            time
                    )
            );
            ReservationJpaEntity reservation = reservationJpaRepository.saveAndFlush(
                    ReservationJpaEntity.from(
                            Reservation.reserve(
                                    reservationName,
                                    slot.toDomain(),
                                    LocalDateTime.of(2027, 8, 18, 12, 0).plusDays(current)
                            ),
                            slot
                    )
            );
            return reservation.getId();
        });
    }

    private synchronized long nextSequence() {
        return sequence++;
    }

    private static class FetchDefaultProbe {
        @ManyToOne
        @JoinColumn(name = "many_to_one_id")
        private Object manyToOne;

        @OneToMany
        private List<Object> oneToMany;
    }

}
