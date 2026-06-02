package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingResult;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    Optional<ReservationWaitingResult> findWaitingById(Long id);

    List<Reservation> findAll(int page, int size);

    List<ReservationWaitingResult> findAllByGuestName(String guestName);

    List<ReservationWaitingResult> findAllByGuestNameExceptCanceled(String guestName);

    Reservation save(Reservation reservation);

    boolean updateSlot(Long id, ReservationSlot slot, Status status);

    boolean cancelById(Long id);

    boolean updateStatus(Long id, Status status);

    Optional<Long> findFirstWaitingIdBySlotForUpdate(ReservationSlot slot);

    boolean existsBySlotAndGuestNameExceptCanceled(ReservationSlot slot, String guestName);

    boolean existsReservationBySlot(ReservationSlot slot);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
