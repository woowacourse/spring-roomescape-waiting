package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    List<Reservation> findByReservationInfoDateAndReservationInfoThemeId(LocalDate date, Long themeId);

    @Query("""
                SELECT r FROM Reservation r
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                  AND (:themeId IS NULL OR r.reservationInfo.theme.id = :themeId)
                  AND (:fromDate IS NULL OR r.reservationInfo.date >= :fromDate)
                  AND (:endDate IS NULL OR r.reservationInfo.date <= :endDate)
            """)
    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("endDate") LocalDate endDate
    );

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByReservationInfoReservationTimeId(Long timeId);

    boolean existsByReservationInfoDateAndReservationInfoReservationTimeStartAt(LocalDate date, LocalTime startAt);

    boolean existsByReservationInfoThemeId(Long themeId);
}
