package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findReservationsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findReservationsByDateBetweenAndThemeIdAndMemberId(LocalDate dateBefore, LocalDate dateAfter,
                                                                         long themeId, long memberId);

    boolean existsByTimeId(Long id);

    List<Reservation> findReservationsByMemberId(long id);

    @Query(
            value = """
            insert into reservation (date, time_id, theme_id, member_id)
            values (:date, :timeId, :themeId, :memberId)
            """,
            nativeQuery = true
    )
    @Modifying
    @Transactional
    void saveWaiting(@Param("date") LocalDate date,
                     @Param("timeId") Long timeId,
                     @Param("themeId") Long themeId,
                     @Param("memberId") Long memberId);
}
