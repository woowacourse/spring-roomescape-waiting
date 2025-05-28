package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.MyReservationAndWaitingResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.service.ScheduleService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.service.WaitingService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReservationServiceFacade {
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ScheduleService scheduleService;
    private final WaitingService waitingService;

    public ReservationServiceFacade(ReservationService reservationService, MemberService memberService, ReservationTimeService reservationTimeService, ThemeService themeService, ScheduleService scheduleService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.scheduleService = scheduleService;
        this.waitingService = waitingService;
    }

    @Transactional
    public ReservationResponse createReservation(
            ReservationCreateRequest reservationCreateRequest,
            MemberPrincipal memberPrincipal
    ) {
        ReservationTime reservationTime = reservationTimeService.getReservationTimeByTimeId(reservationCreateRequest.timeId());
        Theme theme = themeService.getByThemeId(reservationCreateRequest.themeId());
        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);

        Schedule savedSchedule;

        boolean isExists = scheduleService.existsByDateAndTimeIdAndThemeId(reservationCreateRequest.date(), reservationTime.getId(), theme.getId());
        if (isExists) {
            savedSchedule = scheduleService.getScheduleByDateAndTimeIdAndThemeId(reservationCreateRequest.date(), reservationTime.getId(), theme.getId());
            boolean existsWaiting = waitingService.existsBySchedule(savedSchedule);
            if (existsWaiting) {
                return createWaiting(reservationCreateRequest, member, savedSchedule);
            }
        } else {
            savedSchedule = scheduleService.createSchedule(reservationCreateRequest.date(), reservationTime, theme);
        }

        List<ReservationTime> availableTimes = reservationTimeService.findByReservationDateAndThemeId(
                reservationCreateRequest.date(),
                reservationCreateRequest.themeId()
        );

        return reservationService.createReservation(
                member,
                availableTimes,
                savedSchedule,
                reservationCreateRequest
        );
    }

    private ReservationResponse createWaiting(ReservationCreateRequest reservationCreateRequest, Member member, Schedule savedSchedule) {
        validateConflict(member, savedSchedule);
        Waiting waiting = waitingService.createWaiting(reservationCreateRequest.toWaitingCreateRequest(), savedSchedule, member);
        return ReservationResponse.from(waiting);
    }

    private void validateConflict(Member member, Schedule savedSchedule) {
        boolean isConflictReservation = reservationService.existsByMemberAndSchedule(member, savedSchedule);
        if (isConflictReservation) {
            throw new ConflictException("내 예약에 대기 요청 할 수 없습니다.");
        }

        boolean isConflictWaiting = waitingService.existsByMemberAndSchedule(member, savedSchedule);
        if (isConflictWaiting) {
            throw new ConflictException("이미 대기 내역이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    @Transactional
    public void deleteReservationById(Long id) {
        reservationService.deleteReservationById(id);
    }

    @Transactional(readOnly = true)
    public List<MyReservationAndWaitingResponse> findAllMyReservationAndWaiting(MemberPrincipal memberPrincipal) {
        return Stream.of(findAllMyReservation(memberPrincipal), findAllMyWaitingWithRank(memberPrincipal))
                .flatMap(Collection::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationAndWaitingResponse> findAllMyWaitingWithRank(MemberPrincipal memberPrincipal) {
        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);
        return waitingService.getMyReservationAndWaitingResponseByMemberId(member.getId());
    }

    @Transactional(readOnly = true)
    public List<MyReservationAndWaitingResponse> findAllMyReservation(MemberPrincipal memberPrincipal) {
        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);
        return reservationService.findAllByMember(member);
    }
}
