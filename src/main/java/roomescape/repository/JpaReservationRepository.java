package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaitingRank;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findReservationByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findReservationsByDateBetweenAndThemeIdAndMemberId(LocalDate dateBefore, LocalDate dateAfter,
                                                                         long themeId, long memberId);

    List<Reservation> findReservationsByMemberId(long id);

    @Query(
        "select new roomescape.domain.reservation.ReservationWaitingRank(" +
        "cast(count(r) as int)) " +
        "from Reservation r " +
        "where r.status = 'WAITING' " +
        "and r.theme.id = :themeId " +
        "and r.date = :date " +
        "and r.time.id = :timeId"
    )
    ReservationWaitingRank findWaitingRankByThemeIdAndDateAndTimeId(
        @Param("themeId") Long themeId,
        @Param("date") LocalDate date,
        @Param("timeId") Long timeId
    );
}
