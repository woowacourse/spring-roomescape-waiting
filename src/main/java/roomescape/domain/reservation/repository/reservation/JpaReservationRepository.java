package roomescape.domain.reservation.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTheme_IdAndMember_IdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                               LocalDate dateTo);

    boolean existsByDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByDateAndTheme_Id(LocalDate date, Long themeId);

    List<Reservation> findByMember_Id(Long memberId);
}
