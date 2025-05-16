package roomescape.reservation.application.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
                SELECT r FROM Reservation r
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                  AND (:themeId IS NULL OR r.theme.id = :themeId)
                  AND (:fromDate IS NULL OR r.date >= :fromDate)
                  AND (:endDate IS NULL OR r.date <= :endDate)
            """)
    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            Long memberId,
            Long themeId,
            LocalDate fromDate,
            LocalDate endDate
    );

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByDateAndReservationTimeStartAt(LocalDate date, LocalTime startAt);

    boolean existsByThemeId(Long themeId);
}
