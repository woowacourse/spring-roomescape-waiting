package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;

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

    @Query("""
                select r
                from Reservation r
                join fetch r.reservationTime t
                join fetch r.theme th
                join fetch r.member m
                where m.id = :memberId
            """)
    List<Reservation> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("""
                select r
                from Reservation r
                join fetch r.reservationTime t
                join fetch r.theme th
                join fetch r.member m
                where r.reservationDate.reservationDate between :startDate and :endDate
            """)
    List<Reservation> findAllByReservationDateBetween(
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end
    );

    @Query("""
                select r
                from Reservation r
                join fetch r.reservationTime t
                join fetch r.theme th
                join fetch r.member m
            """)
    List<Reservation> findAll(
    );

    @Query("""
                select r
                from Reservation r
                join fetch r.reservationTime t
                join fetch r.theme th
                join fetch r.member m
                where r.id = :reservationId
            """)
    Optional<Reservation> findById(
            @Param("reservationId") Long id
    );

}
