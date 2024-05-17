package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    @Query("""
            SELECT r FROM Reservation AS r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:memberId IS NULL OR r.member.id = :memberId)
            AND (:startDate IS NULL OR r.date >= :startDate)
            AND (:endDate IS NULL OR r.date <= :endDate)
            """)
    List<Reservation> findAllByCondition(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    List<Reservation> findByMemberId(Long memberId);
}
