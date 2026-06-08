package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.common.dto.PageResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    Optional<ReservationWaitingDto> findWaitingById(Long id);

    PageResult<Reservation> findAllByStatusCanceledNot(int page, int size);

    List<ReservationWaitingDto> findWaitingAllByGuestName(String guestName);

    Optional<Reservation> findBySlotAndStatusWaitingAndWaitingNumberIsOne(LocalDate date, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    boolean updateSlotAndStatus(Long id, Long slotId, Status status, LocalDateTime lastModifiedAt);

    boolean updateStatus(Long id, Status status);

    boolean cancelById(Long id);

    boolean existsBySlotAndGuestNameExceptCanceled(ReservationSlot slot, String guestName);

    boolean existsBySlotAndStatusConfirmed(ReservationSlot reservationSlot);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
