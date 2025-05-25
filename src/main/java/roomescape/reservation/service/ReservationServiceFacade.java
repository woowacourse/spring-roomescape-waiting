package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.exception.BadRequestException;
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
        // todo 검증 위치들에 대해서 생각해 보기 (책임, 통일성 등,,)
        ReservationTime reservationTime = reservationTimeService.findById(reservationCreateRequest.timeId())
                .orElseThrow(() -> new BadRequestException("올바른 예약 시간을 찾을 수 없습니다."));

        Theme theme = themeService.findById(reservationCreateRequest.themeId())
                .orElseThrow(() -> new BadRequestException("올바른 방탈출 테마가 없습니다."));

        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);

        Schedule schedule = new Schedule(null, reservationCreateRequest.date(), reservationTime, theme);

        boolean isExists = scheduleService.existsByDateAndTimeIdAndThemeId(reservationCreateRequest.date(), reservationTime.getId(), theme.getId());

        Schedule savedSchedule;
        if (isExists) {
            savedSchedule = scheduleService.findByDateAndTimeIdAndThemeId(reservationCreateRequest.date(), reservationTime.getId(), theme.getId())
                    .orElseThrow(() -> new BadRequestException("예약 가능한 일정이 존재하지 않습니다."));

            boolean existsWaiting = waitingService.existsBySchedule(savedSchedule);

            if (existsWaiting) {
                // todo WaitingService 안에서 중복 문제 발생
                boolean isConflictReservation = reservationService.existsByMemberAndSchedule(member, savedSchedule);
                if (isConflictReservation) {
                    throw new ConflictException("내 예약에 대기 요청 할 수 없습니다.");
                }

                boolean isConflictWaiting = waitingService.existsByMemberAndSchedule(member, savedSchedule);
                if (isConflictWaiting) {
                    throw new ConflictException("이미 대기 내역이 존재합니다.");
                }

                Waiting waiting = waitingService.createWaiting(reservationCreateRequest.toWaitingCreateRequest(), savedSchedule, member);
                return ReservationResponse.from(waiting);
            }

        } else {
            savedSchedule = scheduleService.save(schedule);
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

    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    // todo 예약을 삭제하게 된다면 예약 삭제 후 대기열을 확인해서 예약으로 올리는 작업이 있어야 한다.
    public void deleteReservationById(Long id) {
        reservationService.deleteReservationById(id);
    }

    public List<MyReservationAndWaitingResponse> findAllMyReservationAndWaiting(MemberPrincipal memberPrincipal) {
        return Stream.of(findAllMyReservation(memberPrincipal), findAllMyWaitingWithRank(memberPrincipal))
                .flatMap(Collection::stream)
                .toList();
    }

    public List<MyReservationAndWaitingResponse> findAllMyWaitingWithRank(MemberPrincipal memberPrincipal) {
        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);
        return waitingService.findWaitingWithRankByMemberId(member.getId());
    }

    public List<MyReservationAndWaitingResponse> findAllMyReservation(MemberPrincipal memberPrincipal) {
        Member member = memberService.findExistingMemberByPrincipal(memberPrincipal);
        return reservationService.findAllByMember(member);
    }
}
