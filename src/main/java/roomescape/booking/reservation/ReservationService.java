package roomescape.booking.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.reservation.dto.AdminFilterReservationRequest;
import roomescape.booking.reservation.dto.AdminReservationRequest;
import roomescape.booking.reservation.dto.ReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleService;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.schedule.PastScheduleException;
import roomescape.member.Member;
import roomescape.member.MemberService;

import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {

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

    @Transactional(readOnly = true)
    public List<ReservationResponse> readAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> readAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        return reservationRepository.findAllByMember_IdAndSchedule_Theme_IdAndSchedule_DateBetween(
                        request.memberId(), request.themeId(),
                        request.from(), request.to()
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByEmail(final String email) {
        Member member = memberService.findByEmail(email);
        return reservationRepository.findAllByMember(member);
    }

    @Transactional
    public void deleteById(final Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void save(final Reservation reservation) {
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation findById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
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
