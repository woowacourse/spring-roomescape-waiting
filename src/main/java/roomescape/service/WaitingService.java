package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.WaitingResponse;
import roomescape.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class WaitingService {
    private final MemberRepository memberRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(MemberRepository memberRepository, TimeSlotRepository timeSlotRepository,
                          ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                          ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResponse create(WaitingRequest waitingRequest) {
        Member member = findMemberById(waitingRequest.memberId());
        TimeSlot timeSlot = findTimeSlotById(waitingRequest.timeId());
        Theme theme = findThemeById(waitingRequest.themeId());

        validate(waitingRequest.date(), timeSlot, theme, member);

        Waiting waiting = waitingRequest.toEntity(member, timeSlot, theme);
        Waiting createdWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(createdWaiting);
    }

    public List<WaitingResponse> findEntireReservationList() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public List<ReservationMineResponse> findMyWaitings(LoginMember loginMember) {
        Member member = findMemberById(loginMember.id());
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberIdByDateAsc(member);
        return waitings.stream()
                .map(ReservationMineResponse::from)
                .toList();
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

    private Member findMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 회원 입니다"));
    }

    private TimeSlot findTimeSlotById(long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 시간 입니다"));
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 테마 입니다"));
    }
}
