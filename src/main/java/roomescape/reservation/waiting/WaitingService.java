package roomescape.reservation.waiting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsScheduleException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.exception.custom.reason.schedule.ScheduleNotExistException;
import roomescape.exception.custom.reason.waiting.WaitingNotFoundException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservation.waiting.dto.WaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.schedule.Schedule;
import roomescape.schedule.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public WaitingResponse create(final WaitingRequest request, final LoginMember loginMember) {
        final Schedule schedule = scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(request.date(), request.timeId(), request.themeId())
                .orElseThrow(ScheduleNotExistException::new);

        // 과거 예약인지 확인
        validatePastDateTime(schedule.getDate(), schedule.getReservationTime());

        // 예약이 존재하는지 확인 -> 스케줄에 status 만들기?
        boolean isReservationExist = reservationRepository.existsBySchedule(schedule);
        if (!isReservationExist) {
            throw new ReservationNotExistsScheduleException();
        }

        // 예약 있으면 스케줄에 대한 웨이팅 생성
        final Member member = getMemberByEmail(loginMember.email());
        final Long rank = getWaitingRank(schedule);
        final Waiting waiting = new Waiting(schedule, member, rank);
        final Waiting savedWaiting = waitingRepository.save(waiting);

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
        if (!isAuthorized(member, waiting.getMember())) {
            throw new AuthorizationException();
        }

        waitingRepository.delete(waiting);

        Long rank = waiting.getRank();
        List<Waiting> waitingsAfterRank = waitingRepository.findWaitingGreaterThanRank(waiting.getSchedule(), rank);
        waitingsAfterRank.forEach(Waiting::decrementRank);
    }

    @Transactional
    public void deleteById(final Long id) {
        Waiting waiting = getWaitingById(id);
        waitingRepository.delete(waiting);

        Long rank = waiting.getRank();
        List<Waiting> waitingsAfterRank = waitingRepository.findWaitingGreaterThanRank(waiting.getSchedule(), rank);
        waitingsAfterRank.forEach(Waiting::decrementRank);
    }

    private boolean isAuthorized(final LoginMember member, final Member waitingMember) {
        return waitingMember.isEmailEquals(member.email());
    }

    private Long getWaitingRank(final Schedule schedule) {
        final List<Waiting> waitings = waitingRepository.findAllBySchedule(schedule);
        return (long) waitings.size();
    }

    private void validatePastDateTime(final LocalDate date, ReservationTime reservationTime) {
        final LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new ReservationPastDateException();
        }
        if (date.isEqual(today)) {
            validatePastTime(reservationTime);
        }
    }

    private void validatePastTime(final ReservationTime reservationTime) {
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new ReservationPastTimeException();
        }
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
