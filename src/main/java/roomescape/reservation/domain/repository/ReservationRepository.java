package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Collection<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate dateFrom,
                                                                      LocalDate dateTo);

    boolean existsByDateAndTimeId(LocalDate reservationDate, Long id);

    Collection<Reservation> findAllByMemberId(Long memberId);
}
