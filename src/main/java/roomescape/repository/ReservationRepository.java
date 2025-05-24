package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationWithRank;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndThemeIdAndStatus(LocalDate date, Long themeId, ReservationStatus status);

    List<Reservation> findAllByDateBetween(LocalDate start, LocalDate end);

    @Query(value = """
                select r from Reservation r
                where (:memberId is null or r.member.id  = :memberId)
                and (:themeId is null or r.theme.id = :themeId)
                and (:dateFrom is null or r.date >= :dateFrom)
                and (:dateTo is null or r.date <= :dateTo)
            """)
    List<Reservation> findAllByFilter(@Param("memberId") Long memberId,
                                      @Param("themeId") Long themeId,
                                      @Param("dateFrom") LocalDate dateFrom,
                                      @Param("dateTo") LocalDate dateTo);

    @Query(value = """
            select r
            from Reservation r
            join fetch r.member
            join fetch r.reservationTime
            join fetch r.theme
            where r.status = :status
            """)
    List<Reservation> findAllFetchByStatus(@Param("status") ReservationStatus status);

    Optional<Reservation> findByDateAndReservationTimeAndThemeAndStatus(LocalDate date,
                                                                        ReservationTime reservationTime,
                                                                        Theme theme,
                                                                        ReservationStatus status);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    @Query(value = """
            select new roomescape.domain.ReservationWithRank(r,
                        (select count(rw)
                         from Reservation rw
                         where rw.theme = r.theme
                         and rw.date = r.date
                         and rw.reservationTime = r.reservationTime
                         and rw.status = :status
                         and rw.createAt <= r.createAt))
            from Reservation r
            where r.member.id = :memberId
            """)
    List<ReservationWithRank> findReservationWithRank(@Param("memberId") Long memberId,
                                                      @Param("status") ReservationStatus status);
}
