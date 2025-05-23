package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationStatusRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    List<Reservation> findByStatus(ReservationStatus status);

    Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status);

    @Query("""
            select r from Reservation r
            join fetch r.reservationDateTime.reservationTime t
            join fetch r.theme th
            join fetch r.reserver m
            where (:themeId is null or r.theme.id = :themeId)
              and (:memberId is null or r.reserver.id = :memberId)
              and (:fromDate is null or r.reservationDateTime.reservationDate.date >= :fromDate)
              and (:toDate is null or r.reservationDateTime.reservationDate.date <= :toDate)
              and (r.status = :status)
            """)
    Page<Reservation> findFilteredReservations(@Param("themeId") Long themeId, @Param("memberId") Long memberId,
                                               @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                                               @Param("status") ReservationStatus status, Pageable pageable
    );

    @Query("""
            select r
            from Reservation r
            join fetch r.reserver
            join fetch r.reservationDateTime.reservationTime
            join fetch r.theme
            where r.reserver.id = :memberId
            and r.status = :status
            """)
    List<Reservation> findByMemberIdAndStatus(@Param("memberId") Long memberId,
                                              @Param("status") ReservationStatus status);

    @Query("""
            select exists
            (select r from Reservation r
                where r.reserver.id = :memberId
                and r.reservationDateTime.reservationDate.date = :date
                and r.reservationDateTime.reservationTime.id = :timeId
                and r.status = :status
            )
            """)
    boolean existsByMemberIdAndDateAndTimeIdAndStatus(@Param("memberId") Long memberId,
                                                      @Param("date") LocalDate date,
                                                      @Param("timeId") Long timeId,
                                                      @Param("status") ReservationStatus status
    );

    @Query("""
            select exists
                (select r from Reservation r
                where r.reservationDateTime.reservationDate.date = :date
                and r.reservationDateTime.reservationTime.id = :timeId
                and r.status = :status)
            """)
    boolean existsByDateAndTimeIdAndStatus(@Param("date") LocalDate date,
                                           @Param("timeId") Long timeId,
                                           @Param("status") ReservationStatus status);

    @Query("""
            select r
            from Reservation r
            join fetch r.reservationDateTime.reservationTime t
            join fetch r.theme th
            join fetch r.reserver m
            where r.reservationDateTime.reservationDate.date = :date
              and t.id = :timeId
              and r.status = :status
            """)
    List<Reservation> findByDateAndTimeIdAndStatus(@Param("date") LocalDate date,
                                                   @Param("timeId") Long timeId,
                                                   @Param("status") ReservationStatus status
    );
}
