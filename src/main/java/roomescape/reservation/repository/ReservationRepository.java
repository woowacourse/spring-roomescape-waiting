package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.common.domain.ReservationSlot;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationIdResponse;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByName(String name);

    void update(Long id, LocalDate date, Long timeId);

    boolean existsBySlot(ReservationSlot slot);

    boolean existsByNameAndDateAndTimeIdAndThemeId(String name, LocalDate date, Long timeId, Long themeId);

    boolean existsBySlotExcludingId(ReservationSlot slot, Long id);

    void deleteById(Long id);

    ReservationIdResponse findReservationId(LocalDate date, Long themeId, Long timeId);
}