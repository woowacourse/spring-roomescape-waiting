package roomescape.booking;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.dto.BookingResponse;
import roomescape.booking.reservation.Reservation;
import roomescape.booking.reservation.ReservationRepository;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.waiting.Waiting;
import roomescape.booking.waiting.WaitingRepository;
import roomescape.exception.custom.reason.member.MemberNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class BookingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public List<BookingResponse> readAllByMember(final LoginMember loginMember) {
        Member member = getMemberByEmail(loginMember.email());
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<Waiting> waitings = waitingRepository.findAllByMember(member);

        return Stream.concat(
                reservations.stream().map(BookingResponse::of),
                waitings.stream().map(BookingResponse::of)
        ).toList();
    }

    @Transactional
    public void deleteReservationById(final Long id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.deleteById(id);

        Schedule schedule = reservation.getSchedule();

        List<Waiting> waitings = waitingRepository.findAllBySchedule(schedule);
        if (waitings.isEmpty()) {
            return;
        }

        changeFirstWaitingToReservation(waitings);
        waitings.forEach(Waiting::decrementRank);
    }

    private void changeFirstWaitingToReservation(final List<Waiting> waitings) {
        Waiting firstWaiting = waitings.getFirst();
        Reservation reservation = new Reservation(firstWaiting.getMember(), firstWaiting.getSchedule());
        reservationRepository.save(reservation);
        waitingRepository.delete(firstWaiting);
    }

    private Reservation getReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}
