package roomescape.domain.reservation.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.domain.reservation.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void deleteById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
