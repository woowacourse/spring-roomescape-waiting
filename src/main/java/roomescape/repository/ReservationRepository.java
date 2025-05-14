package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndTheme_Id(LocalDate date, Long themeId);

    List<Reservation> findAllByDateBetween(LocalDate end, LocalDate start);

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

    boolean existsByDateAndReservationTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);

    boolean existsByTime_Id(Long timeId); //TODO: 검증필요

    boolean existsByTheme_Id(Long themeId); //TODO: 검증필요
}
