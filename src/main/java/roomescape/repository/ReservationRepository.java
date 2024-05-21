package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(long memberId, long themeId,
                                                             LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByMemberId(long memberId);

    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByReservationTimeId(long timeId);

    @Query("""
            SELECT CASE 
            WHEN COUNT(r) > 0 
            THEN true 
            ELSE false END 
            FROM Reservation r 
            WHERE r.member = :member 
            AND r.theme = :theme 
            AND r.reservationTime = :reservationTime 
            AND r.date = :date
            """)
    boolean hasBookedReservation(
            @Param("member") Member member,
            @Param("theme") Theme theme,
            @Param("reservationTime") ReservationTime reservationTime,
            @Param("date") LocalDate date);
}

