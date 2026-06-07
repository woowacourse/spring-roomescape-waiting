package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.presentation.reservation.request.AdminReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationUpdateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
import roomescape.presentation.reservation.response.ReservationUpdateResponse;
import roomescape.presentation.reservation.response.ReservationsResponse;
import roomescape.presentation.reservation.response.UserReservationsResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ReservationsResponse getAllReservations() {
        return ReservationsResponse.of(reservationRepository.findAll());
    }

    public UserReservationsResponse getUserReservations(String username) {
        User user = findByUsernameOrThrow(username);
        List<Reservation> userReservations = reservationRepository.findAllReservationsByUserId(user.getId());
        return UserReservationsResponse.of(username, userReservations);
    }

    @Transactional
    public ReservationCreateResponse createReservationByUser(ReservationCreateRequest request, String username) {
        User user = findByUsernameOrThrow(username);
        ReservationSlot slot = findSlotByIdForUpdateOrThrow(request.slotId());
        LocalDateTime now = LocalDateTime.now(clock);
        validateReservable(slot, user, now);

        Reservation savedReservation = reservationRepository.save(Reservation.create(user, slot, now));

        recalculateReservationsForSlot(slot);
        return ReservationCreateResponse.from(savedReservation);
    }

    @Transactional
    public ReservationCreateResponse createReservationByAdmin(AdminReservationCreateRequest request) {
        User user = findByUsernameOrThrow(request.username());
        ReservationSlot slot = findSlotByIdOrThrow(request.slotId());
        LocalDateTime now = LocalDateTime.now(clock);
        validateReservationNotDuplicated(slot, user);

        Reservation savedReservation = reservationRepository.save(Reservation.create(user, slot, now));

        recalculateReservationsForSlot(slot);
        return ReservationCreateResponse.from(savedReservation);
    }

    @Transactional
    public ReservationUpdateResponse updateReservationByUser(
            Long id,
            ReservationUpdateRequest request,
            String username
    ) {
        LocalDateTime now = LocalDateTime.now(clock);

        Reservation reservation = findReservationByIdAndUsernameOrThrow(id, username);
        
        ReservationSlot currentSlot = reservation.getSlot();
        currentSlot.validateIsNotInPast(now);

        ReservationSlot targetSlot = findSlotByIdOrThrow(request.slotId());
        validateSameReservationSlot(currentSlot, targetSlot);

        User user = findByUsernameOrThrow(username);
        validateReservable(targetSlot, user, now);

        Reservation updatedReservation = reservationRepository.update(reservation.moveTo(targetSlot, now));

        recalculateReservationsForSlot(currentSlot);
        recalculateReservationsForSlot(targetSlot);

        return ReservationUpdateResponse.from(updatedReservation);
    }

    @Transactional
    public ReservationUpdateResponse updateReservationByAdmin(Long id, ReservationUpdateRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);

        Reservation reservation = findByIdOrThrow(id);
        ReservationSlot currentSlot = reservation.getSlot();

        ReservationSlot targetSlot = findSlotByIdOrThrow(request.slotId());
        validateSameReservationSlot(currentSlot, targetSlot);

        validateReservationNotDuplicated(targetSlot, reservation.getUser());

        Reservation updatedReservation = reservationRepository.update(reservation.moveTo(targetSlot, now));

        recalculateReservationsForSlot(currentSlot);
        recalculateReservationsForSlot(targetSlot);

        return ReservationUpdateResponse.from(updatedReservation);
    }

    @Transactional
    public void deleteReservationByAdmin(Long id) {
        Reservation reservation = findByIdOrThrow(id);
        reservationRepository.deleteById(id);
        recalculateReservationsForSlot(reservation.getSlot());
    }

    @Transactional
    public void cancelReservationByUser(Long id, String username) {
        Reservation reservation = findReservationByIdAndUsernameOrThrow(id, username);
        reservation.validateCancellable(LocalDateTime.now(clock));
        reservationRepository.deleteById(id);
        recalculateReservationsForSlot(reservation.getSlot());
    }

    private Reservation findReservationByIdAndUsernameOrThrow(Long id, String username) {
        return reservationRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void recalculateReservationsForSlot(ReservationSlot slot) {
        List<Reservation> reservations = reservationRepository.findAllBySlotIdOrderByReservedAt(slot.getId());
        if (reservations.isEmpty()) {
            return;
        }
        reservationRepository.batchUpdate(assignWaitingNumbersAndStatuses(reservations));
    }

    private List<Reservation> assignWaitingNumbersAndStatuses(List<Reservation> reservations) {
        List<Reservation> updatedReservations = new ArrayList<>();

        updatedReservations.add(reservations.getFirst().updateConfirmed());
        for (int index = 1; index < reservations.size(); index++) {
            updatedReservations.add(reservations.get(index).updateWaiting(index));
        }

        return updatedReservations;
    }

    private User findByUsernameOrThrow(String username) {
        return userRepository.findByName(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Reservation findByIdOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private ReservationSlot findSlotByIdOrThrow(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_SLOT_NOT_FOUND));
    }

    private ReservationSlot findSlotByIdForUpdateOrThrow(Long slotId) {
        return slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_SLOT_NOT_FOUND));
    }

    private void validateReservable(ReservationSlot slot, User user, LocalDateTime now) {
        slot.validateIsNotInPast(now);
        validateReservationNotDuplicated(slot, user);
    }

    private void validateReservationNotDuplicated(ReservationSlot slot, User user) {
        if (reservationRepository.existsBySlotIdAndUserId(slot.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateSameReservationSlot(ReservationSlot currentSlot, ReservationSlot targetSlot) {
        if (currentSlot.getId().equals(targetSlot.getId())) {
            throw new BusinessException(ErrorCode.RESERVATION_SAME_SLOT);
        }
    }
}
