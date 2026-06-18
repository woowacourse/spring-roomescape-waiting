package roomescape.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.domain.payment.PaymentOrder;
import roomescape.dto.command.CancelReservationCommand;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.response.ReservationWithStatusResponses;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.ReservationPaymentResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingReservationException;
import roomescape.exception.PastDateTimeReservationException;
import roomescape.exception.PastReservationModificationException;
import roomescape.exception.ReservationConcurrentConflictException;
import roomescape.exception.ReservationNotFoundForWaitingException;
import roomescape.exception.ReservationNotReservedException;
import roomescape.exception.ReservationNotWaitingException;
import roomescape.exception.ReservationOwnerMismatchException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;
import roomescape.repository.PaymentOrderRepository;
import roomescape.service.payment.IdempotencyKeyGenerator;
import roomescape.service.payment.OrderIdGenerator;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderIdGenerator orderIdGenerator;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final TimeProvider timeProvider;

    public ReservationService(ReservationRepository reservationRepository,
                              ThemeRepository themeRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              UserRepository userRepository,
                              StoreRepository storeRepository,
                              PaymentOrderRepository paymentOrderRepository,
                              OrderIdGenerator orderIdGenerator,
                              IdempotencyKeyGenerator idempotencyKeyGenerator,
                              TimeProvider timeProvider) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.orderIdGenerator = orderIdGenerator;
        this.idempotencyKeyGenerator = idempotencyKeyGenerator;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("예약", id));
    }

    @Transactional(readOnly = true)
    public ReservationWithStatusResponses getMyReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId).stream()
                .filter(Reservation::isReserved)
                .toList();

        Map<Reservation, Integer> waitingReservations =
                reservationRepository.findWaitingReservationsWithOrderByUserId(userId);

        return ReservationWithStatusResponses.of(reservations, waitingReservations, false);
    }

    @Transactional
    public ReservationPaymentResponse createReservation(CreateReservationCommand command) {
        Reservation newReservation = buildReservation(command, ReservationStatus.PAYMENT_PENDING);

        validateNotPastDateTime(newReservation);
        validateNotDuplicated(newReservation);

        Long newReservationId = reservationRepository.save(newReservation);
        String orderId = orderIdGenerator.generate();
        String idempotencyKey = idempotencyKeyGenerator.generate();
        paymentOrderRepository.save(new PaymentOrder(null, newReservationId, orderId, command.amount(), null,
                idempotencyKey));
        return new ReservationPaymentResponse(newReservationId, orderId, command.amount());
    }

    @Transactional
    public Reservation createWaitingReservation(CreateReservationCommand command) {
        validateReservationAlreadyExists(command);

        Reservation newWaitingReservation = buildReservation(command, ReservationStatus.WAITING);

        validateNotPastDateTime(newWaitingReservation);

        validateNotDuplicatedWaiting(newWaitingReservation);

        Long newReservationId = reservationRepository.save(newWaitingReservation);
        return newWaitingReservation.withId(newReservationId);
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
    public void cancelOwnReservation(CancelReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("예약", command.reservationId()));
        validateReservationOwner(command.userId(), reservation);
        validateIsReserved(reservation);
        validateExistingNotInPast(reservation);

        Optional<Reservation> firstWaitingReservation = findFirstWaitingReservationForUpdate(reservation);

        deleteReservation(command.reservationId());

        firstWaitingReservation.ifPresent(this::confirmWaitingReservation);
    }

    @Transactional
    public void cancelOwnWaitingReservation(CancelReservationCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("예약", command.reservationId()));
        validateReservationOwner(command.userId(), reservation);
        validateIsWaiting(reservation);
        validateExistingNotInPast(reservation);

        deleteReservation(command.reservationId());
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
        return new Reservation(null, user, theme, command.date(), reservationTime, store, status);
    }

    private void validateIsReserved(Reservation existing) {
        if (!existing.isReserved()) {
            throw new ReservationNotReservedException(existing.getStatus().toString());
        }
    }

    private void validateIsWaiting(Reservation existing) {
        if (!existing.isWaiting()) {
            throw new ReservationNotWaitingException(existing.getStatus().toString());
        }
    }

    private void validateReservationAlreadyExists(CreateReservationCommand command) {
        Boolean isReservedExist = reservationRepository.existsReservedByDateAndTimeAndThemeAndStore(
                command.date(), command.timeId(), command.themeId(), command.storeId()
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
        if (reservationRepository.existsReservedOrPaymentPendingByDateAndTimeAndThemeAndStore(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(),
                reservation.getStore().getId())) {
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

    private Optional<Reservation> findFirstWaitingReservationForUpdate(Reservation reservation) {
        return reservationRepository.findFirstWaitingReservationByDateAndTimeAndThemeAndStoreForUpdate(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getStore().getId());
    }

    private void deleteReservation(long reservationId) {
        int affected = reservationRepository.deleteById(reservationId);
        validateReservationModified(affected);
    }

    private void confirmWaitingReservation(Reservation waitingReservation) {
        Reservation confirmedReservation = waitingReservation.confirm();
        updateWaitingToReserved(confirmedReservation);
    }

    private void updateWaitingToReserved(Reservation reservation) {
        int affected = reservationRepository.updateWaitingToReserved(reservation);
        validateReservationModified(affected);
    }

    private void validateReservationModified(int affected) {
        if (affected == 0) {
            throw new ReservationConcurrentConflictException();
        }
    }
}
