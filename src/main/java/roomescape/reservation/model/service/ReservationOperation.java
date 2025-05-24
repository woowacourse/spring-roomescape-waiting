package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.vo.Schedule;

@Component
@RequiredArgsConstructor
public class ReservationOperation {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationValidator reservationValidator;

    @Transactional
    public Reservation reserve(Schedule schedule, Long memberId) {
        reservationValidator.validateNoDuplication(schedule);
        ReservationDetails reservationDetails = createReservationDetails(schedule, memberId);
        Reservation reservation = Reservation.createFuture(reservationDetails);
        Reservation savedReservation = reservationRepository.save(reservation);
        return savedReservation;
    }

    @Transactional
    public void cancel(Reservation reservation) {
        reservation.changeToCancel();
        reservationWaitingRepository.findFirstPendingBySchedule(reservation.getSchedule())
                .ifPresent(reservationWaiting -> {
            reservationWaiting.changeToAccept();
            Reservation newReservation = Reservation.confirmedFromWaiting(reservationWaiting);
            reservationRepository.save(newReservation);
        });
    }

    private ReservationDetails createReservationDetails(Schedule schedule, Long memberId) {
        ReservationTime reservationTime = reservationTimeRepository.getById(schedule.timeId());
        ReservationTheme reservationTheme = reservationThemeRepository.getById(schedule.themeId());
        Member member = memberRepository.getById(memberId);

        return ReservationDetails.builder()
                .date(schedule.date())
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .member(member)
                .build();
    }
}
