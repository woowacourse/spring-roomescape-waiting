package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByDateAndReservationTime(LocalDate date, ReservationTime time);

    List<Reservation> findByTheme_IdAndMember_IdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateAfter,
            LocalDate dateBefore);

    List<Reservation> findByTheme_IdAndDate(final Long theme_id, final LocalDate date);

    List<Reservation> findByMember_Id(Long id);

    List<Reservation> findByThemeId(Long id);

    List<Reservation> findByReservationTimeId(Long id);
}
