package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byFilter;
import static roomescape.infrastructure.ReservationSpecs.bySlot;
import static roomescape.infrastructure.ReservationSpecs.byStatus;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Queues;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWithOrder;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reservation reserve(final long userId, final LocalDate date, final long timeId, final long themeId) {
        var slot = toReservationSlot(date, timeId, themeId);
        throwIfDuplicates(slot);

        var user = userRepository.getById(userId);
        var reservation = new Reservation(user, slot);
        user.reserve(reservation);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation waitFor(final long userId, final LocalDate date, final long timeId, final long themeId) {
        var slot = toReservationSlot(date, timeId, themeId);
        var user = userRepository.getById(userId);

        var reservation = Reservation.ofWaiting(user, slot);
        user.reserve(reservation);
        return reservationRepository.save(reservation);
    }

    private ReservationSlot toReservationSlot(final LocalDate date, final long timeId, final long themeId) {
        var timeSlot = timeSlotRepository.getById(timeId);
        var theme = themeRepository.getById(themeId);
        return ReservationSlot.forReserve(date, timeSlot, theme);
    }

    private void throwIfDuplicates(final ReservationSlot slot) {
        if (reservationRepository.exists(bySlot(slot))) {
            throw new AlreadyExistedException("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
        }
    }

    public List<Reservation> findAllReservations(ReservationSearchFilter filter) {
        return reservationRepository.findAll(byFilter(filter));
    }

    public List<ReservationWithOrder> findAllWaitings() {
        var allWaitings = reservationRepository.findAll(byStatus(ReservationStatus.WAITING));
        var queues = new Queues(allWaitings);
        return queues.orderOfAll(allWaitings);
    }

    public void removeByIdForce(final long id) {
        reservationRepository.deleteByIdOrElseThrow(id);
    }

    @Transactional
    public void cancelWaiting(final long userId, final long reservationId) {
        var user = userRepository.getById(userId);
        var reservation = reservationRepository.getById(reservationId);
        user.cancelReservation(reservation);
    }
}
