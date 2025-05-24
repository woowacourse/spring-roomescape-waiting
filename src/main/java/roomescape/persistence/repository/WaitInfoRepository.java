package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.business.domain.WaitInfo;

public interface WaitInfoRepository extends JpaRepository<WaitInfo, Long> {

    @Query("""
                SELECT w
                FROM WaitInfo w
                WHERE (:rank IS NULL OR w.rank = :rank)
                    AND (:memberId IS NULL OR w.member.id = :memberId)
                    AND (:themeId IS NULL OR w.reservation.theme.id = :themeId)
                    AND (:startDate IS NULL OR w.reservation.date >= :startDate)
                    AND (:endDate IS NULL OR w.reservation.date <= :endDate)
            """)
    List<WaitInfo> filterByMemberIdAndThemeIdAndStartDateAndEndDateAndRank(
            Long memberId,
            Long themeId,
            LocalDate startDate,
            LocalDate endDate,
            Long rank
    );

    List<WaitInfo> findByMemberId(Long memberId);

    boolean existsByReservationId(Long reservationId);

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    boolean existsByIdAndMemberId(Long waitInfoId, Long memberId);

    Long countByIdLessThanEqualAndReservationId(Long id, Long reservationId);

    List<WaitInfo> findByRankNot(Long rank);

    List<WaitInfo> findByRank(Long rank);

    Long countByReservationId(Long reservationId);
}
