package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byFilter;
import static roomescape.infrastructure.ReservationSpecs.bySchedule;
import static roomescape.infrastructure.ReservationSpecs.byStatus;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.RoomescapeSchedule;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationQueues;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWithOrder;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reservation reserve(final long userId, final LocalDate date, final long timeId, final long themeId) {
        var schedule = toRoomescapeSchedule(date, timeId, themeId);
        if (reservationRepository.exists(bySchedule(schedule))) {
            throw new AlreadyExistedException("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
        }

        return reserve(userId, schedule, ReservationStatus.RESERVED);
    }

    @Transactional
    public Reservation waitFor(final long userId, final LocalDate date, final long timeId, final long themeId) {
        var schedule = toRoomescapeSchedule(date, timeId, themeId);
        if (!reservationRepository.exists(bySchedule(schedule))) {
            throw new BusinessRuleViolationException("해당 날짜, 시간, 테마에 예약이 없습니다. 바로 예약해 주세요.");
        }

        return reserve(userId, schedule, ReservationStatus.WAITING);
    }

    public List<Reservation> findAllReservations(ReservationSearchFilter filter) {
        return reservationRepository.findAll(byFilter(filter));
    }

    public List<ReservationWithOrder> findAllWaitings() {
        var allWaitings = reservationRepository.findAll(byStatus(ReservationStatus.WAITING));
        var queues = new ReservationQueues(allWaitings);
        return queues.orderOfAll(allWaitings);
    }

    @Transactional
    public void removeById(final long id) {
        var reservation = reservationRepository.getById(id);
        if (reservation.isReserved()) {
            var queues = reservationRepository.findQueuesBySchedules(List.of(reservation.reservedSchedule()));
            var nextReservation = queues.findNext(reservation);
            nextReservation.ifPresent(Reservation::confirm);
        }
        reservationRepository.delete(reservation);
    }

    @Transactional
    public void cancelWaiting(final long userId, final long reservationId) {
        var user = userRepository.getById(userId);
        var reservation = reservationRepository.getById(reservationId);
        user.cancelReservation(reservation);
    }

    private Reservation reserve(final long userId, final RoomescapeSchedule schedule, final ReservationStatus status) {
        var user = userRepository.getById(userId);
        var reservation = new Reservation(user, schedule, status);
        user.reserve(reservation);
        return reservationRepository.save(reservation);
    }

    private RoomescapeSchedule toRoomescapeSchedule(final LocalDate date, final long timeId, final long themeId) {
        var timeSlot = timeSlotRepository.getById(timeId);
        var theme = themeRepository.getById(themeId);
        return RoomescapeSchedule.forReserve(date, timeSlot, theme);
    }
}
