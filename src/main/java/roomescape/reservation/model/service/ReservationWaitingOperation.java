package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.dto.ReservationWaitingDetails;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.vo.Schedule;

@Component
@RequiredArgsConstructor
public class ReservationWaitingOperation {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationWaitingValidator reservationWaitingValidator;

    public void waiting(Schedule schedule, Long memberId) {
        ReservationWaitingDetails reservationWaitingDetails = createReservationWaitingDetails(schedule,
                memberId);
        reservationValidator.validateExistenceBySchedule(schedule);
        reservationWaitingValidator.validateAlreadyWaiting(schedule, memberId);
        ReservationWaiting reservationWaiting = ReservationWaiting.createFuture(reservationWaitingDetails);
        reservationWaitingRepository.save(reservationWaiting);
    }

    private ReservationWaitingDetails createReservationWaitingDetails(Schedule schedule, Long memberId) {
        ReservationTime reservationTime = reservationTimeRepository.getById(schedule.timeId());
        ReservationTheme reservationTheme = reservationThemeRepository.getById(schedule.themeId());
        Member member = memberRepository.getById(memberId);
        return ReservationWaitingDetails.builder()
                .date(schedule.date())
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .member(member)
                .build();
    }
}
