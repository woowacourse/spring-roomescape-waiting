package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findByTimeId(long timeId);

    List<Reservation> findByThemeId(long themeId);

    List<Reservation> findByMemberId(long memberId);

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findByDateAndThemeId(LocalDate date, long themeId);

    @Query("""
            SELECT r FROM Reservation AS r
            LEFT JOIN FETCH r.theme
            LEFT JOIN FETCH r.member
            LEFT JOIN FETCH r.time
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:memberId IS NULL OR r.member.id = :memberId)
            AND (:status IS NULL OR r.status = :status)
            AND (:dateFrom IS NULL OR r.date >= :dateFrom)
            AND (:dateTo IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findByThemeIdAndMemberIdAndStatusAndDateBetween(Long themeId, Long memberId,
                                                                      Optional<Status> status,
                                                                      LocalDate dateFrom, LocalDate dateTo);

    @Modifying
    @Query("delete from Reservation where id = :id")
    int deleteById(@Param("id") long id);
}
