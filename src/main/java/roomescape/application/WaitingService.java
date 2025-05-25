package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.exception.DuplicateWaitingException;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.WaitingRequest;
import roomescape.presentation.dto.response.WaitingResponse;
import roomescape.presentation.dto.response.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeService themeService;
    private final ReservationTimeService timeService;
    private final MemberService memberService;

    public WaitingService(WaitingRepository waitingRepository, ThemeService themeService, ReservationTimeService timeService, MemberService memberService) {
        this.waitingRepository = waitingRepository;
        this.themeService = themeService;
        this.timeService = timeService;
        this.memberService = memberService;
    }

    @Transactional
    public WaitingResponse createWaiting(WaitingRequest waitingRequest, LoginMember loginMember) {
        validateDuplicateWaiting(waitingRequest.date(), waitingRequest.themeId(), waitingRequest.timeId(), loginMember.id());
        Member member = memberService.findMemberById(loginMember.id());
        Theme theme = themeService.findThemeById(waitingRequest.themeId());
        ReservationTime reservationTime = timeService.findReservationTimeById(waitingRequest.timeId());

        Waiting waiting = Waiting.from(waitingRequest.date(), member, theme, reservationTime);
        Waiting savedWaiting = waitingRepository.save(waiting);

        return WaitingResponse.from(savedWaiting);
    }

    public List<WaitingWithRank> getMyWaitingsWithRank(LoginMember loginMember) {
        return waitingRepository.findWaitingsWithRankByMemberId(loginMember.id());
    }

    @Transactional
    public void deleteWaiting(Long id) {
        Waiting waiting = findWaitingById(id);
        waitingRepository.delete(waiting);
    }

    public List<WaitingResponse> getAllWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::from)
                .toList();
    }

    private Waiting findWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약 대기를 찾을 수 없습니다. : " + id));
    }

    private void validateDuplicateWaiting(LocalDate date, Long themeId, Long timeId, Long memberId) {
        boolean isAlreadyWaited = waitingRepository.existsByDateAndThemeIdAndTimeIdAndMemberId(date, themeId, timeId, memberId);
        if (isAlreadyWaited) {
            throw new DuplicateWaitingException("[ERROR] 이미 예약 대기를 요청한 상태입니다.");
        }
    }
}
