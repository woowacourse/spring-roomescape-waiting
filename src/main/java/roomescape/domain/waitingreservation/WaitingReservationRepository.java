package roomescape.domain.waitingreservation;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.waitingreservation.dto.RankProjection;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {

    boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, Long dateId, Long timeId, Long themeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select wr
        from WaitingReservation wr
        join fetch wr.date
        join fetch wr.time
        join fetch wr.theme
        where wr.date.id = :dateId and wr.time.id = :timeId and wr.theme.id = :themeId
        order by wr.createdAt asc, wr.id asc
        limit 1
    """)
    Optional<WaitingReservation> findOldestBySlot(Long dateId, Long timeId, Long themeId);

    List<WaitingReservation> findAllByName(String name);

    @Query(value = """
        select wr.id, row_number() over (
            partition by wr.date_id, wr.time_id, wr.theme_id
            order by wr.created_at asc, wr.id asc
        ) as rank
        from waiting_reservation wr
        join (
            select date_id, time_id, theme_id
            from waiting_reservation
            where name = :name
        ) slot on wr.date_id = slot.date_id and wr.time_id = slot.time_id and wr.theme_id = slot.theme_id
    """, nativeQuery = true)
    List<RankProjection> findRankByName(String name);
}
