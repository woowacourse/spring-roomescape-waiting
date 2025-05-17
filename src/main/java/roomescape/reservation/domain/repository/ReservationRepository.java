package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    Collection<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate from,
                                                                      LocalDate to);

    boolean existsByDateAndTimeId(LocalDate reservationDate, Long id);

    Collection<Reservation> findAllByMemberId(Long memberId);

    Collection<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);
}
