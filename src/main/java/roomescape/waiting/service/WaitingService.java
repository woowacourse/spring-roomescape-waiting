package roomescape.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.controller.request.WaitingRequest;
import roomescape.waiting.controller.response.WaitingResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberQueryService memberQueryService;

    public WaitingResponse wait(WaitingRequest request, Long memberId) {
        LocalDate date = request.date();
        Long timeId = request.timeId();
        Long themeId = request.themeId();

        ReservationDateTime reservationDateTime = ReservationDateTime.create(
                new ReservationDate(date), reservationTimeService.getReservationTime(timeId));
        Theme theme = themeService.getTheme(themeId);
        Member waiter = memberQueryService.getMember(memberId);

        validateDuplicateReservation(date, timeId, themeId, waiter.getId());

        LocalDateTime waitedAt = LocalDateTime.now();
        Waiting waiting = Waiting.wait(waiter, reservationDateTime, theme, waitedAt);
        Waiting saved = waitingRepository.save(waiting);

        return WaitingResponse.from(saved);
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId, Long memberId) {
        if (waitingRepository.existsBy(date, timeId, themeId, memberId)) {
            throw new InvalidArgumentException("이미 대기 중인 예약입니다.");
        }
    }
}
