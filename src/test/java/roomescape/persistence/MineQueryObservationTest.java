package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservation.jpa.ReservationJpaEntity;
import roomescape.repository.reservation.jpa.ReservationJpaRepository;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaEntity;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaRepository;
import roomescape.repository.reservationwaiting.jpa.ReservationWaitingJpaEntity;
import roomescape.repository.reservationwaiting.jpa.ReservationWaitingJpaRepository;
import roomescape.repository.reservationwaiting.jpa.WaitingWithRank;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaRepository;
import roomescape.repository.theme.jpa.ThemeJpaEntity;
import roomescape.repository.theme.jpa.ThemeJpaRepository;

@SpringBootTest
class MineQueryObservationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    private ReservationSlotJpaRepository reservationSlotJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private ReservationWaitingJpaRepository reservationWaitingJpaRepository;

    @Test
    void mine_list_without_fetch_join_triggers_lazy_selects_for_each_item() {
        String ownerName = uniqueName("mine-n1");
        createMineFixture(ownerName, 2, 2);

        Statistics statistics = statistics();
        statistics.clear();

        new TransactionTemplate(transactionManager).execute(status -> {
            List<ReservationJpaEntity> reservations = entityManager.createQuery(
                            "select r from ReservationJpaEntity r where r.name = :name order by r.id",
                            ReservationJpaEntity.class
                    )
                    .setParameter("name", ownerName)
                    .getResultList();

            List<ReservationWaitingJpaEntity> waitings = entityManager.createQuery(
                            "select w from ReservationWaitingJpaEntity w where w.name = :name order by w.id",
                            ReservationWaitingJpaEntity.class
                    )
                    .setParameter("name", ownerName)
                    .getResultList();

            List<String> dtoValues = Stream.concat(
                    reservations.stream().map(this::toDtoValue),
                    waitings.stream().map(this::toDtoValue)
            ).toList();

            assertThat(dtoValues).hasSize(4);
            return null;
        });

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(14L);
    }

    @Test
    void mine_list_with_fetch_join_collapses_to_two_selects() {
        String ownerName = uniqueName("mine-fetch");
        createMineFixture(ownerName, 2, 2);

        Statistics statistics = statistics();
        statistics.clear();

        new TransactionTemplate(transactionManager).execute(status -> {
            List<ReservationJpaEntity> reservations = entityManager.createQuery("""
                            select distinct r
                            from ReservationJpaEntity r
                            join fetch r.slot s
                            join fetch s.theme
                            join fetch s.time
                            where r.name = :name
                            order by r.id
                            """, ReservationJpaEntity.class
                    )
                    .setParameter("name", ownerName)
                    .getResultList();

            List<ReservationWaitingJpaEntity> waitings = entityManager.createQuery("""
                            select distinct w
                            from ReservationWaitingJpaEntity w
                            join fetch w.slot s
                            join fetch s.theme
                            join fetch s.time
                            where w.name = :name
                            order by w.id
                            """, ReservationWaitingJpaEntity.class
                    )
                    .setParameter("name", ownerName)
                    .getResultList();

            List<String> dtoValues = Stream.concat(
                    reservations.stream().map(this::toDtoValue),
                    waitings.stream().map(this::toDtoValue)
            ).toList();

            assertThat(dtoValues).hasSize(4);
            return null;
        });

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);
    }

    @Test
    void jpql_subquery_can_calculate_waiting_rank() {
        String ownerName = uniqueName("mine-rank");
        createWaitingRankFixture(ownerName);

        List<WaitingWithRank> results = reservationWaitingJpaRepository.findAllWithRankByName(ownerName);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).rank()).isEqualTo(2L);
    }

    private void createMineFixture(final String ownerName, final int reservationCount, final int waitingCount) {
        new TransactionTemplate(transactionManager).execute(status -> {
            for (int index = 0; index < reservationCount; index++) {
                createReservation(ownerName, index);
            }

            for (int index = 0; index < waitingCount; index++) {
                createWaiting(ownerName, reservationCount + index);
            }

            return null;
        });
    }

    private void createWaitingRankFixture(final String ownerName) {
        new TransactionTemplate(transactionManager).execute(status -> {
            ThemeJpaEntity theme = themeJpaRepository.saveAndFlush(
                    ThemeJpaEntity.from(Theme.createNew(
                            ownerName + "-rank-theme",
                            "rank-description",
                            "rank-thumbnail"
                    ))
            );
            ReservationTimeJpaEntity time = reservationTimeJpaRepository.saveAndFlush(
                    ReservationTimeJpaEntity.from(
                            ReservationTime.createNew(LocalTime.of(13, 30))
                    )
            );
            ReservationSlotJpaEntity slot = reservationSlotJpaRepository.saveAndFlush(
                    ReservationSlotJpaEntity.from(
                            new ReservationSlot(
                                    LocalDate.of(2027, 11, 1),
                                    theme.toDomain(),
                                    time.toDomain()
                            ),
                            theme,
                            time
                    )
            );

            createWaitingOnSlot(slot, "before-" + ownerName, LocalDateTime.of(2027, 9, 1, 14, 0));
            createWaitingOnSlot(slot, ownerName, LocalDateTime.of(2027, 9, 1, 14, 1));
            createWaitingOnSlot(slot, "after-" + ownerName, LocalDateTime.of(2027, 9, 1, 14, 2));

            return null;
        });
    }

    private ReservationJpaEntity createReservation(final String ownerName, final int offset) {
        ThemeJpaEntity theme = themeJpaRepository.saveAndFlush(
                ThemeJpaEntity.from(Theme.createNew(
                        ownerName + "-theme-" + offset,
                        "description-" + offset,
                        "thumbnail-" + offset
                ))
        );
        ReservationTimeJpaEntity time = reservationTimeJpaRepository.saveAndFlush(
                ReservationTimeJpaEntity.from(
                        ReservationTime.createNew(LocalTime.of(10, 10 + offset))
                )
        );
        ReservationSlotJpaEntity slot = reservationSlotJpaRepository.saveAndFlush(
                ReservationSlotJpaEntity.from(
                        new ReservationSlot(
                                LocalDate.of(2027, 10, 1).plusDays(offset),
                                theme.toDomain(),
                                time.toDomain()
                        ),
                        theme,
                        time
                )
        );
        return reservationJpaRepository.saveAndFlush(
                ReservationJpaEntity.from(
                        Reservation.reserve(
                                ownerName,
                                slot.toDomain(),
                                LocalDateTime.of(2027, 9, 1, 12, 0).plusMinutes(offset)
                        ),
                        slot
                )
        );
    }

    private ReservationWaitingJpaEntity createWaiting(final String ownerName, final int offset) {
        ThemeJpaEntity theme = themeJpaRepository.saveAndFlush(
                ThemeJpaEntity.from(Theme.createNew(
                        ownerName + "-theme-" + offset,
                        "description-" + offset,
                        "thumbnail-" + offset
                ))
        );
        ReservationTimeJpaEntity time = reservationTimeJpaRepository.saveAndFlush(
                ReservationTimeJpaEntity.from(
                        ReservationTime.createNew(LocalTime.of(11, 10 + offset))
                )
        );
        ReservationSlotJpaEntity slot = reservationSlotJpaRepository.saveAndFlush(
                ReservationSlotJpaEntity.from(
                        new ReservationSlot(
                                LocalDate.of(2027, 10, 20).plusDays(offset),
                                theme.toDomain(),
                                time.toDomain()
                        ),
                        theme,
                        time
                )
        );

        ReservationWaiting waiting = ReservationWaiting.createNew(
                slot.toDomain(),
                ownerName,
                LocalDateTime.of(2027, 9, 1, 13, 0).plusMinutes(offset)
        );

        return reservationWaitingJpaRepository.saveAndFlush(
                ReservationWaitingJpaEntity.from(waiting, slot)
        );
    }

    private ReservationWaitingJpaEntity createWaitingOnSlot(
            final ReservationSlotJpaEntity slot,
            final String ownerName,
            final LocalDateTime requestedAt
    ) {
        ReservationWaiting waiting = ReservationWaiting.createNew(
                slot.toDomain(),
                ownerName,
                requestedAt
        );

        return reservationWaitingJpaRepository.saveAndFlush(
                ReservationWaitingJpaEntity.from(waiting, slot)
        );
    }

    private String toDtoValue(final ReservationJpaEntity reservation) {
        return reservation.getSlot().getTheme().getName() + ":" + reservation.getSlot().getTime().getStartAt();
    }

    private String toDtoValue(final ReservationWaitingJpaEntity waiting) {
        return waiting.getSlot().getTheme().getName() + ":" + waiting.getSlot().getTime().getStartAt();
    }

    private Statistics statistics() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        return statistics;
    }

    private synchronized String uniqueName(final String prefix) {
        return prefix + "-" + nextSequence();
    }

    private static long sequence;

    private static synchronized long nextSequence() {
        return sequence++;
    }
}
