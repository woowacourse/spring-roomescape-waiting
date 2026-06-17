package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.dto.ReservationDetail;

public interface ReservationRepository {

    List<ReservationDetail> findAll();

    List<Reservation> findByName(String name);

    Optional<Reservation> findById(Long id);

    Optional<ReservationDetail> findDetailById(Long id);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    Integer delete(Long id);

    Boolean existsByNameAndDateAndThemeAndTime(String name, LocalDate date, Long themeId, Long timeId);

    Boolean existsByDateAndThemeAndTimeExcludingId(LocalDate date, Long themeId, Long timeId, Long id);

    boolean insertFromOldestWaiting(LocalDate date, Long themeId, Long timeId);

    boolean confirmPayment(Long reservationId);
}
