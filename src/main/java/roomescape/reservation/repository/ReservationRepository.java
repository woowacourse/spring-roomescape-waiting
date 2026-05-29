package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationIdResponse;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByName(String name);

    void update(Long id, LocalDate date, Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdExcludingId(LocalDate date, Long timeId, Long themeId, Long id);

    void deleteById(Long id);

    ReservationIdResponse findReservationId(LocalDate date, Long themeId, Long timeId);
}