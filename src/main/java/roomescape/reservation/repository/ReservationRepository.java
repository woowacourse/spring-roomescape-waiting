package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    Optional<ReservationWaitingDto> findWaitingById(Long id);

    List<Reservation> findAll(int page, int size);

    List<ReservationWaitingDto> findWaitingAllByGuestName(String guestName);

    Optional<Reservation> findBySlotAndStatusWaitingAndWaitingNumberIsOne(LocalDate date, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    boolean updateDateAndTimeAndStatus(Long id, LocalDate date, Long timeId, Status status);

    boolean updateStatus(Long id, Status status);

    boolean cancelById(Long id);

    boolean existsBySlotAndGuestNameExceptCanceled(LocalDate date, Long timeId, Long themeId, String guestName);

    boolean existsBySlot(LocalDate date, Long timeId, Long themeId);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
