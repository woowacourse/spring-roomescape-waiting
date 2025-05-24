package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>, ReservationRepository {

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
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

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
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

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Query("""
                select r
                from Reservation r
                join fetch r.reservationTime t
                join fetch r.theme th
                join fetch r.member m
            """)
    List<Reservation> findAll(
    );

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
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

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Query("""
                select th
                from Reservation r
                join r.theme th
                where r.reservationDate.reservationDate between :startDate and :endDate
                group by th
                order by count(r) desc
            """)
    List<Theme> findTopThemesByReservationCount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

}
