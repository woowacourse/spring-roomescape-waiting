package roomescape.repository;


import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.ConfirmedReservation;

public interface ConfirmReservationRepository extends JpaRepository<ConfirmedReservation, Long> {
    List<ConfirmedReservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<ConfirmedReservation> findAllByDateBetween(LocalDate start, LocalDate end);

    @Query(value = """
        select r from Reservation r
        where (:memberId is null or r.member.id  = :memberId)
        and (:themeId is null or r.theme.id = :themeId)
        and (:dateFrom is null or r.date >= :dateFrom)
        and (:dateTo is null or r.date <= :dateTo)
    """)
    List<ConfirmedReservation> findAllByFilter(@Param("memberId") Long memberId,
                                               @Param("themeId") Long themeId,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    boolean existsByTimeIdAndThemeIdAndDate(Long reservationTimeId, Long themeId, LocalDate date);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    List<ConfirmedReservation> findAllByMemberId(Long memberId);
}