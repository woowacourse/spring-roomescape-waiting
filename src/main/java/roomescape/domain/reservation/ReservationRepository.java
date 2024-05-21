package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.schedule.ReservationDate;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Collection;
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

    List<Reservation> findByMemberId(long memberId);

    boolean existsByDetailIdAndMemberId(Long reservationDetailId, Long memberId);

    boolean existsByDetailIdAndStatus(Long reservationDetailId, ReservationStatus status);

    boolean existsByDetailThemeId(long themeId);

    boolean existsByDetailScheduleTimeId(long timeId);

    Optional<Reservation> findFirstByDetailIdOrderByCreatedAt(long detailId);

    List<Reservation> findAllByStatus(ReservationStatus status);
}
