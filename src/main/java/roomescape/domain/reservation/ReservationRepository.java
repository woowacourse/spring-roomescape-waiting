package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.dto.ReservationWithRank;
import roomescape.domain.schedule.ReservationDate;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT r FROM Reservation AS r
            WHERE (:memberId is null or r.member.id = :memberId)
            AND (:themeId is null or r.detail.theme.id = :themeId)
            AND (:dateFrom is null or r.detail.schedule.date >= :dateFrom)
            AND (:dateTo is null or r.detail.schedule.date < :dateTo)""")
    List<Reservation> findBy(@Param("memberId") Long memberId, @Param("themeId") Long themeId,
                             @Param("dateFrom") ReservationDate dateFrom, @Param("dateTo") ReservationDate dateTo);

    boolean existsByDetailIdAndMemberId(Long reservationDetailId, Long memberId);

    boolean existsByDetailIdAndStatus(Long reservationDetailId, ReservationStatus status);

    boolean existsByDetailThemeId(long themeId);

    boolean existsByDetailScheduleTimeId(long timeId);

    Optional<Reservation> findFirstByDetailIdOrderByCreatedAt(long detailId);

    List<Reservation> findAllByStatus(ReservationStatus status);

    @Query("""
            select new roomescape.domain.dto.ReservationWithRank(r,
            (select count(*) from Reservation as cr
            where cr.detail.id = r.detail.id and cr.createdAt < r.createdAt))
            from Reservation r
            where r.member.id = :memberId
            """)
    List<ReservationWithRank> findWithRankingByMemberId(@Param("memberId") long memberId);
}
