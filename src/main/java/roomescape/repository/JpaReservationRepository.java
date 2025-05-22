package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(
            """
             SELECT r FROM Reservation r
             WHERE r.theme.id = :themeId
             AND r.member.id = :memberId
             AND r.date > :dateFrom
             AND r.date < :dateTo
            """
    )
    Optional<List<Reservation>> findReservationsByFilters(
            @Param("themeId") long themeId,
            @Param("memberId") long memberId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );


    Optional<List<Reservation>> findByMemberId(long memberId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
