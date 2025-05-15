package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long id);

    @Query("""
            SELECT r FROM Reservation r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:memberId IS NULL OR r.member.id = :memberId)
              AND (
                (:dateFrom IS NULL AND :dateTo IS NULL)
                OR (:dateFrom IS NULL AND r.date <= :dateTo)
                OR (:dateTo IS NULL AND r.date >= :dateFrom)
                OR (r.date BETWEEN :dateFrom AND :dateTo)
              )
        """)
    List<Reservation> findByMemberAndThemeAndVisitDateBetween(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
    );

    List<Reservation> findAllByMember(Member member);
}
