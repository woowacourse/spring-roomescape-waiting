package roomescape.booking.waiting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.reservation.ReservationService;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleService;
import roomescape.booking.waiting.dto.WaitingRequest;
import roomescape.booking.waiting.dto.WaitingResponse;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsScheduleException;
import roomescape.exception.custom.reason.schedule.PastScheduleException;
import roomescape.member.Member;
import roomescape.member.MemberService;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class WaitingCreateService {

    private final WaitingRepository waitingRepository;
    private final ReservationService reservationService;
    private final ScheduleService scheduleService;
    private final MemberService memberService;

    @Transactional
    public WaitingResponse create(final WaitingRequest request, final LoginMember loginMember) {
        final Schedule schedule = scheduleService.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        validatePast(schedule);
        validateExistsReservationAboutSchedule(schedule);

        Waiting savedWaiting = saveWaiting(loginMember, schedule);
        return WaitingResponse.of(savedWaiting);
    }

    private Waiting saveWaiting(final LoginMember loginMember, final Schedule schedule) {
        final Member member = memberService.findByEmail(loginMember.email());
        final Waiting waiting = new Waiting(schedule, member, LocalDateTime.now());
        return waitingRepository.save(waiting);
    }

    private void validatePast(final Schedule schedule) {
        if (schedule.isPast()) {
            throw new PastScheduleException();
        }
    }

    private void validateExistsReservationAboutSchedule(final Schedule schedule) {
        boolean isReservationExist = reservationService.existsBySchedule(schedule);
        if (!isReservationExist) {
            throw new ReservationNotExistsScheduleException();
        }
    }
}
