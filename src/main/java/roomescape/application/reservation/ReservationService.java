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
import roomescape.domain.reservation.ReservationStatus;
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
        List<Reservation> userReservations = reservationRepository.findAllReservationsByUsername(username);
        return UserReservationsResponse.of(username, userReservations);
    }

    @Transactional
    public ReservationCreateResponse createReservation(ReservationCreateRequest request) {
        User user = userRepository.findByNameOrThrow(request.name());
        LocalDateTime now = LocalDateTime.now(clock);

        ReservationSlot slot = slotRepository.findByScheduleOrThrow(
                request.timeId(),
                request.date(),
                request.themeId()
        );

        slot.validateIsNotInPast(now);

        if (reservationRepository.existsByUserIdAndSlotId(user.getId(), slot.getId())) {
            throw new BusinessException();
        }

        Reservation savedReservation = reservationRepository.save(Reservation.create(user, slot, now));

        reorder(slot);

        return ReservationCreateResponse.from(savedReservation);
    }

    @Transactional
    public void deleteReservationByAdmin(Long id) {
        Reservation reservation = reservationRepository.findByIdOrThrow(id);
        reservationRepository.deleteById(id);
        reorder(reservation.getSlot());
    }

    @Transactional
    public void cancelReservationByUser(Long id) {
        Reservation reservation = reservationRepository.findByIdOrThrow(id);
        reservation.validateCancellable(LocalDateTime.now(clock));
        reservationRepository.deleteById(id);
        reorder(reservation.getSlot());
    }

    private void reorder(ReservationSlot slot) {
        List<Reservation> reservations = reservationRepository.findAllBySlotIdOrderByWaitingNumber(slot.getId());

        if (reservations.isEmpty()) {
            slotRepository.deleteById(slot.getId());
            return;
        }

        List<Reservation> updatedReservations = assignWaitingNumbersAndStatuses(reservations);
        reservationRepository.batchUpdate(updatedReservations);
    }

    private List<Reservation> assignWaitingNumbersAndStatuses(List<Reservation> reservations) {
        List<Reservation> updatedReservations = new ArrayList<>();

        for (int index = 0; index < reservations.size(); index++) {
            Reservation reservation = reservations.get(index);
            ReservationStatus status = determineStatusByOrder(index);

            updatedReservations.add(reservation.update(index, status, clock));
        }

        return updatedReservations;
    }

    private ReservationStatus determineStatusByOrder(int index) {
        if (index == 0) {
            return ReservationStatus.CONFIRMED;
        }
        return ReservationStatus.WAITING;
    }
}
