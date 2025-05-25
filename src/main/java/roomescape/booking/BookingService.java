package roomescape.booking;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.dto.BookingResponse;
import roomescape.booking.reservation.Reservation;
import roomescape.booking.reservation.ReservationService;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.waiting.Waiting;
import roomescape.booking.waiting.WaitingService;

import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class BookingService {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public List<BookingResponse> readAllByMember(final LoginMember loginMember) {
        List<Reservation> reservations = reservationService.findAllByEmail(loginMember.email());
        List<Waiting> waitings = waitingService.findAllByEmail(loginMember.email());

        return Stream.concat(
                reservations.stream().map(BookingResponse::of),
                waitings.stream().map(BookingResponse::of)
        ).toList();
    }

    @Transactional
    public void deleteReservationById(final Long id) {
        Reservation oldReservation = reservationService.findById(id);
        reservationService.deleteById(id);

        Schedule schedule = oldReservation.getSchedule();
        if (!waitingService.existsBySchedule(schedule)) {
            return;
        }

        Waiting firstWaiting = waitingService.findFirstWaitingOfSchedule(schedule);
        waitingService.decreaseRankOfSchedule(schedule);
        changeFirstWaitingToReservation(firstWaiting);
    }

    private void changeFirstWaitingToReservation(final Waiting firstWaiting) {
        waitingService.delete(firstWaiting);
        Reservation reservation = new Reservation(firstWaiting.getMember(), firstWaiting.getSchedule());
        reservationService.save(reservation);
    }
}
