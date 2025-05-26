package roomescape.booking.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.reservation.dto.AdminReservationRequest;
import roomescape.booking.reservation.dto.ReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.member.Member;
import roomescape.member.MemberService;
import roomescape.schedule.Schedule;
import roomescape.schedule.ScheduleService;

@Service
@AllArgsConstructor
public class ReservationCreateService {

    private final ReservationRepository reservationRepository;
    private final ScheduleService scheduleService;
    private final MemberService memberService;

    @Transactional
    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        final Schedule schedule = scheduleService.getByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        validatePast(schedule);
        validateDuplication(schedule);

        final Member member = memberService.getByEmail(loginMember.email());
        final Reservation savedReservation = saveReservation(schedule, member);
        return ReservationResponse.from(savedReservation);
    }

    @Transactional
    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        final Schedule schedule = scheduleService.getByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        validatePast(schedule);
        validateDuplication(schedule);

        final Member member = memberService.getById(request.memberId());
        final Reservation savedReservation = saveReservation(schedule, member);
        return ReservationResponse.from(savedReservation);
    }

    private Reservation saveReservation(final Schedule schedule, final Member member) {
        final Reservation notSavedReservation = new Reservation(member, schedule);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return savedReservation;
    }

    private void validatePast(final Schedule schedule) {
        if (schedule.isPast()) {
            throw new ReservationPastDateException();
        }
    }

    private void validateDuplication(final Schedule schedule) {
        if (reservationRepository.existsBySchedule(schedule)) {
            throw new ReservationConflictException();
        }
    }
}
