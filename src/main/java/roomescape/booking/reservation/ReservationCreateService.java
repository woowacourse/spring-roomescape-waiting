package roomescape.booking.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.reservation.dto.AdminReservationRequest;
import roomescape.booking.reservation.dto.ReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleService;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.schedule.PastScheduleException;
import roomescape.member.Member;
import roomescape.member.MemberService;

@Service
@AllArgsConstructor
public class ReservationCreateService {

    private final ReservationRepository reservationRepository;
    private final ScheduleService scheduleService;
    private final MemberService memberService;

    @Transactional
    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        final Schedule schedule = scheduleService.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        if (schedule.isPast()) {
            throw new PastScheduleException();
        }
        final Member member = memberService.findByEmail(loginMember.email());
        return getReservationResponse(schedule, member);
    }

    @Transactional
    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        final Schedule schedule = scheduleService.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        if (schedule.isPast()) {
            throw new PastScheduleException();
        }
        final Member member = memberService.findById(request.memberId());
        return getReservationResponse(schedule, member);
    }

    private ReservationResponse getReservationResponse(Schedule schedule, final Member member) {
        validateDuplicateReservation(schedule);
        validatePast(schedule);
        final Reservation notSavedReservation = new Reservation(member, schedule);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validatePast(final Schedule schedule) {
        if (schedule.isPast()) {
            throw new ReservationPastDateException();
        }
    }

    private void validateDuplicateReservation(final Schedule schedule) {
        if (reservationRepository.existsBySchedule(schedule)) {
            throw new ReservationConflictException();
        }
    }
}
