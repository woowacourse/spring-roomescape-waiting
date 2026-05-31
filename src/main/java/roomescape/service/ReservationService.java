package roomescape.service;

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
import roomescape.dto.reservation.response.WaitingReservationResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingReservationException;
import roomescape.exception.PastDateTimeReservationException;
import roomescape.exception.PastReservationModificationException;
import roomescape.exception.ReservationNotFoundForWaitingException;
import roomescape.exception.ReservationNotReservedException;
import roomescape.exception.ReservationOwnerMismatchException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.StoreManagementForbiddenException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final TimeProvider timeProvider;

    public ReservationService(ReservationRepository reservationRepository,
                              ThemeRepository themeRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              UserRepository userRepository,
                              StoreRepository storeRepository,
                              TimeProvider timeProvider) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.timeProvider = timeProvider;
    }

    public ReservationResponses getReservations(int page, int size, String name, Long managerId) {
        List<Long> storeIds = storeRepository.findStoreIdsByUserId(managerId);
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
                .orElseThrow(() -> new ResourceNotFoundException("예약", id));
    }

    @Transactional(readOnly = true)
    public ReservationWithStatusResponses getMyReservations(Long userId) {
        Map<Reservation, Integer> myReservations = reservationRepository.findAllByUserIdWithWaitingOrder(userId);

        List<Reservation> reservations = new ArrayList<>();
        Map<Reservation, Integer> waitingReservations = new LinkedHashMap<>();
        for (Map.Entry<Reservation, Integer> entry : myReservations.entrySet()) {
            Reservation reservation = entry.getKey();
            if (reservation.isReserved()) {
                reservations.add(reservation);
                continue;
            }
            if (reservation.isWaiting()) {
                waitingReservations.put(reservation, entry.getValue());
            }
        }

        return ReservationWithStatusResponses.of(reservations, waitingReservations, false);
    }

    @Transactional
    public Reservation createReservation(CreateReservationCommand command) {
        Reservation newReservation = buildReservation(command, ReservationStatus.RESERVED);

        validateNotPastDateTime(newReservation);
        validateNotDuplicated(newReservation);

        Long newReservationId = reservationRepository.save(newReservation);
        return newReservation.withId(newReservationId);
    }

    @Transactional
    public WaitingReservationResponse createWaitingReservation(CreateReservationCommand command) {
        Reservation newWaitingReservation = buildReservation(command, ReservationStatus.WAITING);

        validateReservationIsFullyBooked(command);

        validateNotPastDateTime(newWaitingReservation);

        validateNotDuplicatedWaiting(newWaitingReservation);

        Long newReservationId = reservationRepository.save(newWaitingReservation);
        Reservation saved = newWaitingReservation.withId(newReservationId);

        int waitingOrder = reservationRepository.countWaitingByDateAndTimeAndThemeAndStore(
                saved.getDate(), saved.getTime().getId(), saved.getTheme().getId(), saved.getStore().getId());
        return WaitingReservationResponse.from(saved, waitingOrder);
    }

    @Transactional
    public Reservation updateOwnReservation(UpdateReservationCommand command) {
        Reservation existing = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("예약", command.reservationId()));
        validateReservationOwner(command.userId(), existing);
        validateExistingNotInPast(existing);
        validateIsReserved(existing);

        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new ResourceNotFoundException("테마", command.themeId()));
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("예약 시간", command.timeId()));
        Reservation updated = new Reservation(command.reservationId(), existing.getUser(), theme, command.date(), time,
                existing.getStore(), existing.getStatus());

        validateNotPastDateTime(updated);
        validateNotDuplicatedForUpdate(existing, updated);

        reservationRepository.update(updated);
        return updated;
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long managerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약", reservationId));
        validateManagesStore(managerId, reservation.getStore().getId());
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public void cancelOwnReservation(CancelReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("예약", command.reservationId()));
        validateReservationOwner(command.userId(), reservation);
        validateExistingNotInPast(reservation);

        reservationRepository.deleteById(command.reservationId());
    }

    private Reservation buildReservation(CreateReservationCommand command, ReservationStatus status) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자", command.userId()));
        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new ResourceNotFoundException("테마", command.themeId()));
        ReservationTime reservationTime = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new ResourceNotFoundException("예약 시간", command.timeId()));
        Store store = storeRepository.findById(command.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("매장", command.storeId()));
        Reservation newReservation = new Reservation(null, user, theme, command.date(), reservationTime, store,
                status);
        return newReservation;
    }

    private void validateIsReserved(Reservation existing) {
        if (!existing.isReserved()) {
            throw new ReservationNotReservedException(existing.getStatus().toString());
        }
    }

    private void validateReservationIsFullyBooked(CreateReservationCommand command) {
        Boolean isReservedExist = reservationRepository.existsByDateAndTimeAndThemeAndStoreAndStatus(
                command.date(), command.timeId(), command.themeId(), command.storeId(), ReservationStatus.RESERVED
        );

        if (!isReservedExist) {
            throw new ReservationNotFoundForWaitingException();
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
            throw new StoreManagementForbiddenException();
        }
    }

    private static void validateReservationOwner(Long userId, Reservation reservation) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ReservationOwnerMismatchException();
        }
    }

    private void validateNotPastDateTime(Reservation reservation) {
        if (reservation.isInPast(timeProvider.currentDateTime())) {
            throw new PastDateTimeReservationException();
        }
    }

    private void validateExistingNotInPast(Reservation existing) {
        if (existing.isInPast(timeProvider.currentDateTime())) {
            throw new PastReservationModificationException();
        }
    }

    private void validateNotDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())) {
            throw new DuplicateReservationException();
        }
    }

    private void validateNotDuplicatedWaiting(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndStoreAndUser(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getStore().getId(),
                reservation.getUser().getId())) {
            throw new DuplicateWaitingReservationException();
        }
    }
}
