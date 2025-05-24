package roomescape.waiting.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.user.controller.dto.request.ReservationRequest;
import roomescape.user.controller.dto.response.MemberReservationResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingService(WaitingRepository waitingRepository,
                          MemberService memberService, ReservationTimeService reservationTimeService,
                          ThemeService themeService) {
        this.waitingRepository = waitingRepository;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    public ReservationResponse createById(Long memberId, ReservationRequest request) {
        Member member = memberService.findById(memberId);
        ReservationDate reservationDate = new ReservationDate(request.date());
        ReservationTime reservationTime = reservationTimeService.getReservationTime(request.timeId());
        Theme theme = themeService.getTheme(request.themeId());
        Waiting waiting = Waiting.create(reservationDate, reservationTime, theme, member);
        Waiting created = waitingRepository.save(waiting);

        return ReservationResponse.fromWaiting(created);
    }

    public List<MemberReservationResponse> findAllByMemberId(Long id) {
        return waitingRepository.findAllWaitingWithRankByMemberId(id).stream()
                .map(MemberReservationResponse::fromWaitingWithRank)
                .toList();
    }

    public void deleteById(Long id) {
        Optional<Waiting> waiting = waitingRepository.findById(id);
        if (waiting.isPresent()) {
            waitingRepository.deleteById(id);
            return;
        }
        throw new IllegalArgumentException("[ERROR] 대기를 찾을 수 없습니다.");
    }
}
