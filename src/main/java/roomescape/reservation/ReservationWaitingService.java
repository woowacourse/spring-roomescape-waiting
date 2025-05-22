package roomescape.reservation;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.member.MemberNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.reservation.Reservation;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservation.reservation.dto.ReservationAndWaitingResponse;
import roomescape.reservation.waiting.Waiting;
import roomescape.reservation.waiting.WaitingRepository;
import roomescape.schedule.Schedule;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationWaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public List<ReservationAndWaitingResponse> readAllByMember(final LoginMember loginMember) {
        Member member = getMemberByEmail(loginMember.email());
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<Waiting> waitings = waitingRepository.findAllByMember(member);

        List<ReservationAndWaitingResponse> responses = new ArrayList<>();
        reservations.stream()
                .forEach(reservation -> responses.add(ReservationAndWaitingResponse.of(reservation)));
        waitings.stream()
                .forEach(waiting -> responses.add(ReservationAndWaitingResponse.of(waiting)));

        return responses;
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
