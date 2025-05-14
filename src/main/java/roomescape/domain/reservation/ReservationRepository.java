package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(@Param("timeId") Long reservationTimeId);

    boolean existsByDateAndTimeId(@Param("date") LocalDate reservationDate, @Param("timeId") Long timeId);

    boolean existsByThemeId(@Param("themeId") Long themeId);

    List<Reservation> findByThemeIdAndDate(@Param("themeId") Long themeId, @Param("date") LocalDate reservationDate);

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(@Param("themeId") Long themeId,
                                                             @Param("memberId") Long memberId,
                                                             @Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);

    List<Reservation> findAllByMemberId(@Param("memberId") Long memberId);
}
