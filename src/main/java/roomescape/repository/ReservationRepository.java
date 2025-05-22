package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Reservation> findAllByStatus(ReservationStatus status);

    Optional<Reservation> findByDateAndReservationTimeAndThemeAndStatus(LocalDate date,
                                                                        ReservationTime reservationTime,
                                                                        Theme theme,
                                                                        ReservationStatus status);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    @Query(value = """
                select count(r)
                from Reservation r
                where r.id <= :id
                and r.date = :date
                and r.reservationTime.id = :timeId
                and r.theme.id = :themeId
                and r.status = :status
            """)
    Long countRankById(@Param("id") Long reservationId,
                       @Param("date") LocalDate date,
                       @Param("timeId") Long timeId,
                       @Param("themeId") Long themeId,
                       @Param("status") ReservationStatus status);
}
