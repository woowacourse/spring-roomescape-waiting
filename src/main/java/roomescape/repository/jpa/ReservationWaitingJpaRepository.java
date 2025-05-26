package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationWaiting;

@Repository
public interface ReservationWaitingJpaRepository extends JpaRepository<ReservationWaiting, Long> {

    @Query("""
        SELECT COUNT(rw2) + 1
        FROM ReservationWaiting rw1, ReservationWaiting rw2
        WHERE rw1.id = :id
        AND rw2.date = rw1.date
        AND rw2.time.id = rw1.time.id  
        AND rw2.theme.id = rw1.theme.id
        AND rw2.createdAt < rw1.createdAt
        """)
    int findWaitingOrderById(final long id);

    Optional<ReservationWaiting> findByThemeIdAndTimeIdAndDate(long themeId, long timeId, final LocalDate date);

    Optional<ReservationWaiting> findFirstByThemeIdAndTimeIdAndDateOrderByCreatedAtAsc(
            long themeId, long timeId, final LocalDate date
    );

    boolean existsById(final long id);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(long memberId, long themeId, long timeId, LocalDate date);

    List<ReservationWaiting> findByMemberId(long memberId);

    List<ReservationWaiting> findAll();
}
