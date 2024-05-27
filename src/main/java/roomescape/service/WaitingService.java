package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.WaitingResponse;
import roomescape.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WaitingService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    private final MemberService memberService;
    private final TimeService timeService;
    private final ThemeService themeService;

    public WaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                          MemberService memberService, TimeService timeService, ThemeService themeService) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberService = memberService;
        this.timeService = timeService;
        this.themeService = themeService;
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> findEntireReservations() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationMineResponse> findMyWaitings(LoginMember loginMember) {
        Member member = memberService.findMemberById(loginMember.id());
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberIdByDateAsc(member);
        return waitings.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }

    public WaitingResponse create(WaitingRequest waitingRequest) {
        Member member = memberService.findMemberById(waitingRequest.memberId());
        TimeSlot timeSlot = timeService.findTimeSlotById(waitingRequest.timeId());
        Theme theme = themeService.findThemeById(waitingRequest.themeId());

        validate(waitingRequest.date(), timeSlot, theme, member);

        Waiting waiting = waitingRequest.toEntity(member, timeSlot, theme);
        Waiting createdWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(createdWaiting);
    }

    public void delete(Long id) {
        waitingRepository.deleteById(id);
    }

    private void validate(LocalDate date, TimeSlot timeSlot, Theme theme, Member member) {
        validateReservation(date, timeSlot);
        validateDuplicatedReservation(date, timeSlot, theme, member);
    }

    private void validateReservation(LocalDate date, TimeSlot time) {
        if (time == null || (time.isTimeBeforeNow() && !date.isAfter(LocalDate.now()))) {
            throw new IllegalArgumentException("[ERROR] 지나간 날짜와 시간으로 예약 대기를 걸 수 없습니다.");
        }
    }

    private void validateDuplicatedReservation(LocalDate date, TimeSlot timeSlot, Theme theme, Member member) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndMember(date, timeSlot, theme, member)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약한 테마에 예약 대기를 걸 수 없습니다.");
        }

        if (waitingRepository.existsByDateAndTimeAndThemeAndMember(date, timeSlot, theme, member)) {
            throw new IllegalArgumentException("[ERROR] 예약 대기는 중복으로 신청할 수 없습니다.");
        }
    }
}
