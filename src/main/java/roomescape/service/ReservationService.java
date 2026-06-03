package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.reservation.command.CancelReservationCommand;
import roomescape.dto.reservation.command.CreateReservationCommand;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.command.UpdateReservationCommand;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final StoreRepository storeRepository;
    private final TimeProvider timeProvider;

    public ReservationService(ReservationRepository reservationRepository,
                              ThemeRepository themeRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              StoreRepository storeRepository,
                              TimeProvider timeProvider) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.storeRepository = storeRepository;
        this.timeProvider = timeProvider;
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
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", id));
    }

    @Transactional(readOnly = true)
    public ReservationWithStatusResponses getMyReservations(User user, int page, int size) {
        Map<Reservation, Integer> myReservations = reservationRepository.findAllByUserIdWithWaitingOrder(
                user.getId(), size + 1, page * size);

        boolean hasNext = myReservations.size() > size;

        List<Reservation> reservations = new ArrayList<>();
        Map<Reservation, Integer> waitingReservations = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Reservation, Integer> entry : myReservations.entrySet()) {
            if (count++ >= size) {
                break;
            }
            Reservation reservation = entry.getKey();
            if (reservation.isReserved()) {
                reservations.add(reservation);
                continue;
            }
            if (reservation.isWaiting()) {
                waitingReservations.put(reservation, entry.getValue());
            }
        }

        return ReservationWithStatusResponses.of(reservations, waitingReservations, hasNext);
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
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", command.reservationId()));
        validateReservationOwner(command.user(), existing);
        validateExistingNotInPast(existing);
        validateIsReserved(existing);

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마", command.themeId()));
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간", command.timeId()));
        Reservation updated = new Reservation(command.reservationId(), existing.getUser(), theme, command.date(), time,
                existing.getStore(), existing.getStatus());

        validateNotPastDateTime(updated);
        validateNotDuplicatedForUpdate(existing, updated);

        reservationRepository.update(updated);
        return updated;
    }

    @Transactional
    public void cancelReservation(Long reservationId, User manager) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", reservationId));
        validateManagesStore(manager.getId(), reservation.getStore().getId());
        validateExistingNotInPast(reservation);
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public void deletePastReservation(Long reservationId, User manager) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", reservationId));
        validateManagesStore(manager.getId(), reservation.getStore().getId());
        validateExistingInPast(reservation);
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public void cancelOwnReservation(CancelReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", command.reservationId()));
        validateReservationOwner(command.user(), reservation);
        validateExistingNotInPast(reservation);

        reservationRepository.deleteById(command.reservationId());
    }

    private Reservation buildReservation(CreateReservationCommand command, ReservationStatus status) {
        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마", command.themeId()));
        ReservationTime reservationTime = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간", command.timeId()));
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "매장", command.storeId()));
        return new Reservation(null, command.user(), theme, command.date(), reservationTime, store, status);
    }

    private void validateIsReserved(Reservation existing) {
        if (!existing.isReserved()) {
            throw new RoomescapeException(ErrorType.RESERVATION_NOT_RESERVED, existing.getStatus().toString());
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
        boolean isReservedExist = reservationRepository.existsByDateAndTimeAndThemeAndStoreAndStatus(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getStore().getId(), ReservationStatus.RESERVED
        );

        if (!isReservedExist) {
            throw new RoomescapeException(ErrorType.RESERVATION_NOT_FOUND_FOR_WAITING);
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
            throw new RoomescapeException(ErrorType.STORE_MANAGEMENT_FORBIDDEN);
        }
    }

    private static void validateReservationOwner(User user, Reservation reservation) {
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RoomescapeException(ErrorType.RESERVATION_OWNER_MISMATCH);
        }
    }

    private void validateNotPastDateTime(Reservation reservation) {
        if (reservation.isInPast(timeProvider.currentDateTime())) {
            throw new RoomescapeException(ErrorType.PAST_DATE_TIME_RESERVATION);
        }
    }

    private void validateExistingNotInPast(Reservation existing) {
        if (existing.isInPast(timeProvider.currentDateTime())) {
            throw new RoomescapeException(ErrorType.PAST_RESERVATION_MODIFICATION);
        }
    }

    private void validateExistingInPast(Reservation existing) {
        if (!existing.isInPast(timeProvider.currentDateTime())) {
            throw new RoomescapeException(ErrorType.NON_PAST_RESERVATION_DELETION);
        }
    }

    private void validateNotDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndStore(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getStore().getId())) {
            throw new RoomescapeException(ErrorType.DUPLICATE_RESERVATION);
        }
    }

    private void validateNotDuplicatedWaiting(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndStoreAndUser(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getStore().getId(),
                reservation.getUser().getId())) {
            throw new RoomescapeException(ErrorType.DUPLICATE_WAITING_RESERVATION);
        }
    }
}
