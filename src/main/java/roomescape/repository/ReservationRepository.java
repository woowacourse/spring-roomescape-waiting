package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByDateAndReservationTime(LocalDate date, ReservationTime time);

    Optional<Reservation> findByDateAndThemeIdAndReservationTimeId(
            LocalDate date,
            Long themeId,
            Long reservationTimeId);

    List<Reservation> findAllByMemberId(Long id);

    List<Reservation> findByThemeIdAndDate(Long themeId, LocalDate date);

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateAfter,
            LocalDate dateBefore);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndThemeIdAndReservationTimeId(LocalDate date, Long themeId, Long timeId);
}
