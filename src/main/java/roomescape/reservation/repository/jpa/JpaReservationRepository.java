package roomescape.reservation.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long id);

    @Query("""
                SELECT r FROM Reservation r
                WHERE (:themeId IS NULL OR r.theme.id = :themeId)
                  AND (:memberId IS NULL OR r.member.id = :memberId)
                  AND ((:dateFrom IS NULL OR r.date >= :dateFrom) AND (:dateTo IS NULL OR r.date <= :dateTo))
            """)
    List<Reservation> findByMemberAndThemeAndVisitDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    List<Reservation> findAllByMember(Member member);
}
