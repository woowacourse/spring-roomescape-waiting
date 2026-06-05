package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.presentation.reservation.request.ReservationCreateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
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
        User user = userRepository.findByNameOrThrow(username);
        List<Reservation> userReservations = reservationRepository.findAllReservationsByUserId(user.getId());
        return UserReservationsResponse.of(username, userReservations);
    }

    @Transactional
    public ReservationCreateResponse createReservation(ReservationCreateRequest request) {
        User user = findOrCreateByUsername(request.username()); // TODO: 동시성 문제 발생 가능

        ReservationSlot slot = slotRepository.findByScheduleOrThrow( // TODO: 슬롯 배치 생성 추가
                request.timeId(),
                request.date(),
                request.themeId()
        );

        LocalDateTime now = LocalDateTime.now(clock);
        validateReservable(slot, user, now);

        Reservation savedReservation = reservationRepository.save(Reservation.create(user, slot, now));

        recalculateReservationsForSlot(slot);

        return ReservationCreateResponse.from(savedReservation);
    }

    @Transactional
    public void deleteReservationByAdmin(Long id) {
        Reservation reservation = reservationRepository.findByIdOrThrow(id);
        reservationRepository.deleteById(id);
        recalculateReservationsForSlot(reservation.getSlot());
    }

    @Transactional
    public void cancelReservationByUser(Long id, String username) { // TODO: 이름 확인하기 추가
        Reservation reservation = reservationRepository.findByIdOrThrow(id);
        reservation.validateCancellable(LocalDateTime.now(clock));
        reservationRepository.deleteById(id);
        recalculateReservationsForSlot(reservation.getSlot());
    }

    private User findOrCreateByUsername(String username) {
        return userRepository.findByName(username)
                .orElseGet(() -> userRepository.save(User.create(username)));
    }

    private void validateReservable(ReservationSlot slot, User user, LocalDateTime now) {
        slot.validateIsNotInPast(now);

        if (reservationRepository.existsBySlotIdAndUserId(slot.getId(), user.getId())) { // TODO: 락 추가
            throw new BusinessException();
        }
    }

    private void recalculateReservationsForSlot(ReservationSlot slot) {
        List<Reservation> reservations = reservationRepository.findAllBySlotIdOrderByReservedAt(slot.getId());
        if (reservations.isEmpty()) {
            return;
        }

        List<Reservation> updatedReservations = assignWaitingNumbersAndStatuses(reservations);
        reservationRepository.batchUpdate(updatedReservations);
    }

    private List<Reservation> assignWaitingNumbersAndStatuses(List<Reservation> reservations) {
        List<Reservation> updatedReservations = new ArrayList<>();

        updatedReservations.add(reservations.getFirst().updateConfirmed());

        for (int index = 1; index < reservations.size(); index++) {
            updatedReservations.add(reservations.get(index).updateWaiting(index));
        }

        return updatedReservations;
    }
}
