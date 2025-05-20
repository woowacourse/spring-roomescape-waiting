package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSpec;

public interface ReservationRepository {

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate from,
                                                                LocalDate to);

    boolean existsByDateAndTimeId(LocalDate reservationDate, Long id);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    boolean existsBySpec(ReservationSpec spec);
}
