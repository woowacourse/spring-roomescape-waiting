package roomescape.reservation.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.schedule.ScheduleNotExistException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.reservation.dto.AdminReservationRequest;
import roomescape.reservation.reservation.dto.ReservationRequest;
import roomescape.reservation.reservation.dto.ReservationResponse;
import roomescape.schedule.Schedule;
import roomescape.schedule.ScheduleRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        final Schedule schedule = getSchedule(request.date(), request.timeId(), request.themeId());
        if (schedule.isPast()) {
            throw new ReservationPastDateException();
        }
        final Member member = getMemberByEmail(loginMember.email());
        return getReservationResponse(schedule, member);
    }

    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        final Schedule schedule = getSchedule(request.date(), request.timeId(), request.themeId());
        if (schedule.isPast()) {
            throw new ReservationPastDateException();
        }
        final Member member = getMemberById(request.memberId());
        return getReservationResponse(schedule, member);
    }

    private ReservationResponse getReservationResponse(Schedule schedule, final Member member) {
        validateDuplicateReservation(schedule);
        validatePast(schedule);
        final Reservation notSavedReservation = new Reservation(member, schedule);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> readAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        return reservationRepository.findAllByMember_IdAndSchedule_Theme_IdAndSchedule_DateBetween(
                        request.memberId(), request.themeId(),
                        request.from(), request.to()
                ).stream()
                .map(ReservationResponse::from)
                .toList();
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

    private Schedule getSchedule(final LocalDate date, final Long timeId, final Long themeId) {
        return scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(date, timeId, themeId)
                .orElseThrow(ScheduleNotExistException::new);
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }
}
