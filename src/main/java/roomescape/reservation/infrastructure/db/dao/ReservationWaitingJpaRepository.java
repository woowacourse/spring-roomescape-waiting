package roomescape.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;

public interface ReservationWaitingJpaRepository extends JpaRepository<ReservationWaiting, Long> {

    @Query("""
            SELECT new roomescape.reservation.model.repository.dto.ReservationWaitingWithRank(
            rw,
            (SELECT COUNT(*) + 1
            FROM ReservationWaiting subrw
            WHERE subrw.theme = rw.theme
            AND subrw.date = rw.date
            AND subrw.time = rw.time
            AND subrw.createdAt < rw.createdAt)
            )
            FROM ReservationWaiting rw
            WHERE rw.member.id = :memberId
            """)
    List<ReservationWaitingWithRank> findAllWithRankByMemberId(Long memberId);

    Optional<ReservationWaiting> findByDateAndTimeIdAndThemeId(LocalDate date, Long themeId, Long themeId1);
}
