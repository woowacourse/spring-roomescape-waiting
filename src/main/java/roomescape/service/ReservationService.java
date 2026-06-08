package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.domain.Slot;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.reservation.command.DeleteReservationCommand;
import roomescape.dto.reservation.command.CreateReservationCommand;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.command.UpdateReservationCommand;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final StoreRepository storeRepository;
    private final SlotRepository slotRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ThemeRepository themeRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              StoreRepository storeRepository,
                              SlotRepository slotRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.storeRepository = storeRepository;
        this.slotRepository = slotRepository;
    }

    public ReservationResponses getReservations(int page, int size, String name, User manager) {
        List<Long> storeIds = storeRepository.findStoreIdsByUserId(manager.getId());
        if (storeIds.isEmpty()) {
            return ReservationResponses.of(List.of(), false);
        }
        List<Reservation> reservations = fetchReservations(page, size, name, storeIds);
        boolean hasNext = reservations.size() > size;
        if (hasNext) {
            reservations = reservations.subList(0, size);
        }
        return ReservationResponses.of(reservations, hasNext);
    }

    private List<Reservation> fetchReservations(int page, int size, String name, List<Long> storeIds) {
        if (name == null) {
            return reservationRepository.findAllByStoreIds(storeIds, size + 1, page * size);
        }
        return reservationRepository.findAllByStoreIdsAndName(storeIds, name, size + 1, page * size);
    }

    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=" + id));
    }

    @Transactional(readOnly = true)
    public ReservationWithStatusResponses getMyReservationStatuses(User user, int page, int size) {
        List<ReservationWithWaitingOrder> rows = reservationRepository.findAllByUserIdWithWaitingOrder(
                user.getId(), size + 1, page * size);

        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows = rows.subList(0, size);
        }

        return ReservationWithStatusResponses.of(rows, hasNext);
    }

    @Transactional
    public Reservation create(CreateReservationCommand command, ReservationStatus status) {
        Reservation newReservation = buildReservation(command, status);

        validateNotPastDateTime(newReservation);
        validateCreatable(newReservation, status);

        Long newReservationId = reservationRepository.save(newReservation);
        return newReservation.withId(newReservationId);
    }

    @Transactional
    public Reservation updateOwnReservation(UpdateReservationCommand command) {
        Reservation existing = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=" + command.reservationId()));
        validateReservationOwner(command.user(), existing);
        validateExistingNotInPast(existing);
        validateIsReserved(existing);

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마을(를) 찾을 수 없습니다. id=" + command.themeId()));
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간을(를) 찾을 수 없습니다. id=" + command.timeId()));
        Slot slot = resolveSlot(command.date(), theme, time, existing.getStore());
        Reservation updated = new Reservation(command.reservationId(), existing.getUser(), slot,
                existing.getStatus());

        validateNotPastDateTime(updated);
        validateNotDuplicatedForUpdate(existing, updated);

        reservationRepository.update(updated);
        if (!existing.hasSameSlot(updated)) {
            promoteFirstWaiting(existing.getSlot().getId());
        }
        return updated;
    }

    @Transactional
    public void deleteReservation(Long reservationId, User manager) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=" + reservationId));
        validateManagesStore(manager.getId(), reservation.getStore().getId());
        validateExistingNotInPast(reservation);
        reservationRepository.deleteById(reservationId);
        promoteFirstWaiting(reservation.getSlot().getId());
    }

    @Transactional
    public void deletePastReservation(Long reservationId, User manager) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=" + reservationId));
        validateManagesStore(manager.getId(), reservation.getStore().getId());
        validateExistingInPast(reservation);
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public void deleteOwnReservation(DeleteReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약을(를) 찾을 수 없습니다. id=" + command.reservationId()));
        validateReservationOwner(command.user(), reservation);
        validateExistingNotInPast(reservation);

        reservationRepository.deleteById(command.reservationId());
        promoteFirstWaiting(reservation.getSlot().getId());
    }

    private Reservation buildReservation(CreateReservationCommand command, ReservationStatus status) {
        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마을(를) 찾을 수 없습니다. id=" + command.themeId()));
        ReservationTime reservationTime = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간을(를) 찾을 수 없습니다. id=" + command.timeId()));
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "매장을(를) 찾을 수 없습니다. id=" + command.storeId()));
        Slot slot = resolveSlot(command.date(), theme, reservationTime, store);
        return new Reservation(null, command.user(), slot, status);
    }

    private Slot resolveSlot(LocalDate date, Theme theme, ReservationTime time, Store store) {
        return slotRepository.findByDateAndThemeAndTimeAndStore(date, theme.getId(), time.getId(), store.getId())
                .orElseGet(() -> slotRepository.save(new Slot(null, date, theme, time, store)));
    }

    private void promoteFirstWaiting(Long slotId) {
        if (reservationRepository.existsBySlotIdAndStatus(slotId, ReservationStatus.RESERVED)) {
            return;
        }
        reservationRepository.findFirstWaitingBySlotId(slotId)
                .ifPresent(waiting -> reservationRepository.updateStatus(waiting.getId(), ReservationStatus.RESERVED));
    }

    private void validateIsReserved(Reservation existing) {
        if (!existing.isReserved()) {
            throw new RoomescapeException(ErrorType.RESERVATION_NOT_RESERVED,
                    "해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: " + existing.getStatus());
        }
    }

    private void validateCreatable(Reservation reservation, ReservationStatus status) {
        if (status == ReservationStatus.WAITING) {
            validateReservationIsFullyBooked(reservation);
            validateNotDuplicatedWaiting(reservation);
            return;
        }
        validateNotDuplicated(reservation);
    }

    private void validateReservationIsFullyBooked(Reservation reservation) {
        boolean isReservedExist = reservationRepository.existsBySlotIdAndStatus(
                reservation.getSlot().getId(), ReservationStatus.RESERVED
        );

        if (!isReservedExist) {
            throw new RoomescapeException(ErrorType.RESERVATION_NOT_FOUND_FOR_WAITING,
                    "확정 예약이 없으므로 대기 예약 생성이 불가능합니다.");
        }
    }

    private void validateNotDuplicatedForUpdate(Reservation existing, Reservation updated) {
        if (existing.hasSameSlot(updated)) {
            return;
        }
        validateNotDuplicated(updated);
    }

    private void validateManagesStore(Long managerId, Long storeId) {
        if (!storeRepository.existsByStoreIdAndUserId(storeId, managerId)) {
            throw new RoomescapeException(ErrorType.STORE_MANAGEMENT_FORBIDDEN,
                    "본인이 관리하는 매장의 예약만 관리할 수 있습니다.");
        }
    }

    private static void validateReservationOwner(User user, Reservation reservation) {
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RoomescapeException(ErrorType.RESERVATION_OWNER_MISMATCH,
                    "본인의 예약만 취소 혹은 변경 가능합니다.");
        }
    }

    private void validateNotPastDateTime(Reservation reservation) {
        if (reservation.isInPast(LocalDateTime.now())) {
            throw new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION,
                    "예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다.");
        }
    }

    private void validateExistingNotInPast(Reservation existing) {
        if (existing.isInPast(LocalDateTime.now())) {
            throw new RoomescapeException(ErrorType.PAST_RESERVATION_MODIFICATION,
                    "이미 지난 예약은 수정할 수 없습니다.");
        }
    }

    private void validateExistingInPast(Reservation existing) {
        if (!existing.isInPast(LocalDateTime.now())) {
            throw new RoomescapeException(ErrorType.NON_PAST_RESERVATION_DELETION,
                    "아직 지나지 않은 예약은 삭제할 수 없습니다.");
        }
    }

    private void validateNotDuplicated(Reservation reservation) {
        if (reservationRepository.existsBySlotId(reservation.getSlot().getId())) {
            throw new RoomescapeException(ErrorType.DUPLICATE_RESERVATION,
                    "해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요.");
        }
    }

    private void validateNotDuplicatedWaiting(Reservation reservation) {
        if (reservationRepository.existsBySlotIdAndUserId(
                reservation.getSlot().getId(), reservation.getUser().getId())) {
            throw new RoomescapeException(ErrorType.DUPLICATE_WAITING_RESERVATION,
                    "이미 해당 슬롯에 예약 대기 중입니다.");
        }
    }
}
