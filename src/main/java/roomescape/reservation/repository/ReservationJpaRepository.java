package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.service.ReservationRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>, ReservationRepository {

    @Query("""
                select r
                from Reservation r
                join fetch r.reservationTime t
                join fetch r.theme th
                join fetch r.member u
                where u.id = :userId
                  and th.id = :themeId
                  and r.reservationDate.reservationDate between :startDate and :endDate
            """)
    List<Reservation> findByFilter(
            @Param("userId") Long userId,
            @Param("themeId") Long themeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Reservation> findAllByMemberId(Long memberId);

    Optional<Reservation> findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(Long themeId, Long timeId,
                                                                                              LocalDate date);
    
    boolean existsByReservationDateAndReservationTimeIdAndThemeId(ReservationDate reservationDate, Long timeId,
                                                                  Long themeId);

}
