package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.WaitingRequest;
import roomescape.presentation.dto.response.WaitingResponse;
import roomescape.presentation.dto.response.WaitingWithRank;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeService themeService;
    private final ReservationTimeService timeService;

    public WaitingService(WaitingRepository waitingRepository, ThemeService themeService, ReservationTimeService timeService) {
        this.waitingRepository = waitingRepository;
        this.themeService = themeService;
        this.timeService = timeService;
    }

    @Transactional
    public WaitingResponse createWaiting(WaitingRequest waitingRequest, LoginMember loginMember) {
        Theme theme = themeService.findThemeById(waitingRequest.themeId());
        ReservationTime reservationTime = timeService.findReservationTimeById(waitingRequest.timeId());
        Waiting waiting = Waiting.from(waitingRequest.date(), loginMember.id(), theme, reservationTime);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    public List<WaitingWithRank> getMyWaitingsWithRank(LoginMember loginMember) {
        return waitingRepository.findWaitingsWithRankByMemberId(loginMember.id());
    }
}
