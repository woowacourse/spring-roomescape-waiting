package roomescape.reservation.waiting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsScheduleException;
import roomescape.exception.custom.reason.schedule.PastScheduleException;
import roomescape.exception.custom.reason.schedule.ScheduleNotExistException;
import roomescape.exception.custom.reason.waiting.WaitingNotFoundException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservation.waiting.dto.WaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.schedule.Schedule;
import roomescape.schedule.ScheduleRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public WaitingResponse create(final WaitingRequest request, final LoginMember loginMember) {
        final Schedule schedule = getSchedule(request.date(), request.timeId(), request.themeId());
        validatePast(schedule);
        validateExistsReservationAboutSchedule(schedule);

        Waiting savedWaiting = saveWaiting(loginMember, schedule);
        return WaitingResponse.of(savedWaiting);
    }

    public List<WaitingResponse> readAll() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::of)
                .toList();
    }

    @Transactional
    public void deleteById(final Long id, LoginMember member) {
        Waiting waiting = getWaitingById(id);

        validateAuthorization(member, waiting);

        waitingRepository.delete(waiting);
        decreaseRankOfFollowingWaitings(waiting);
    }

    @Transactional
    public void deleteByIdForAdmin(final Long id) {
        Waiting waiting = getWaitingById(id);
        waitingRepository.delete(waiting);
        decreaseRankOfFollowingWaitings(waiting);
    }

    private Schedule getSchedule(final LocalDate date, final Long timeId, final Long themeId) {
        return scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(date, timeId, themeId)
                .orElseThrow(ScheduleNotExistException::new);
    }

    private Waiting saveWaiting(final LoginMember loginMember, final Schedule schedule) {
        final Member member = getMemberByEmail(loginMember.email());
        final Long rank = getWaitingRank(schedule);
        final Waiting waiting = new Waiting(schedule, member, rank);
        return waitingRepository.save(waiting);
    }

    private void validatePast(final Schedule schedule) {
        if (schedule.isPast()) {
            throw new PastScheduleException();
        }
    }

    private void validateExistsReservationAboutSchedule(final Schedule schedule) {
        boolean isReservationExist = reservationRepository.existsBySchedule(schedule);
        if (!isReservationExist) {
            throw new ReservationNotExistsScheduleException();
        }
    }

    private void validateAuthorization(final LoginMember member, final Waiting waiting) {
        boolean isAuthorized = waiting.getMember().isEmailEquals(member.email());
        if (!isAuthorized) {
            throw new AuthorizationException();
        }
    }

    private Long getWaitingRank(final Schedule schedule) {
        final List<Waiting> waitings = waitingRepository.findAllBySchedule(schedule);
        return (long) waitings.size() + 1;
    }

    private void decreaseRankOfFollowingWaitings(final Waiting waiting) {
        Long rank = waiting.getRank();
        List<Waiting> waitingsAfterRank = waitingRepository.findWaitingGreaterThanRank(waiting.getSchedule(), rank);
        waitingsAfterRank.forEach(Waiting::decrementRank);
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }

    private Waiting getWaitingById(final Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(WaitingNotFoundException::new);
    }
}
