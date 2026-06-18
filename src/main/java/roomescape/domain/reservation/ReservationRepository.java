package roomescape.domain.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Long countByTimeId(Long timeId);

    Long countByDateId(Long dateId);

    @Query("""
        select r.time.id
        from Reservation r
        where r.theme.id = :themeId and r.date.id = :dateId
    """)
    List<Long> findReservedTimes(Long themeId, Long dateId);

    Long countByThemeId(Long id);

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId);

    boolean existsByMemberIdAndDateIdAndTimeIdAndThemeId(Long memberId, Long dateId, Long timeId, Long themeId);
}
