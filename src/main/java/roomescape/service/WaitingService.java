package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.infrastructure.*;
import roomescape.service.request.WaitingAppRequest;
import roomescape.service.response.WaitingAppResponse;
import roomescape.service.response.WaitingWithRankAppResponse;
import roomescape.web.exception.AuthorizationException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public WaitingAppResponse save(WaitingAppRequest request) {
        Waiting waiting = createWaiting(request);
        validateWaiting(waiting);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingAppResponse(savedWaiting);
    }

    private Waiting createWaiting(WaitingAppRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 대기 생성 실패: 사용자를 찾을 수 없습니다 (id: %d)", request.memberId())));
        ReservationDate date = new ReservationDate(request.date());
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 대기 생성 실패: 시간을 찾을 수 없습니다 (id: %d)", request.timeId())));
        Theme theme = themeRepository.findById(request.timeId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 대기 생성 실패: 테마를 찾을 수 없습니다 (id: %d)", request.themeId())));

        return Waiting.createIfFuture(LocalDateTime.now(), member, date, time, theme);
    }

    private void validateWaiting(Waiting waiting) {
        validateReservationExist(waiting);
        validateNotDuplicated(waiting);
    }

    private void validateReservationExist(Waiting waiting) {
        ReservationDate date = waiting.getDate();
        Long timeId = waiting.getTime().getId();
        Long themeId = waiting.getTheme().getId();
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "예약이 존재하지 않는 날짜, 시간, 테마에 대해서는 대기를 생성할 수 없습니다. {date: %s, timeId: %d, themeId: %d}",
                        date.getDate(), timeId, themeId)));

        if (waiting.hasSameMemberWith(reservation)) {
            throw new IllegalArgumentException(String.format(
                    "본인이 예약한 날짜, 시간, 테마에 대해서는 대기를 생성할 수 없습니다. {date: %s, timeId: %d, themeId: %d}",
                    date.getDate(), timeId, themeId));
        }
    }

    private void validateNotDuplicated(Waiting waiting) {
        Long memberId = waiting.getMember().getId();
        ReservationDate date = waiting.getDate();
        Long timeId = waiting.getTime().getId();
        Long themeId = waiting.getTheme().getId();
        boolean isWaitingExist = waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(memberId, date, timeId, themeId);

        if (isWaitingExist) {
            throw new IllegalArgumentException(String.format(
                    "동일한 사용자의 중복된 예약 대기를 생성할 수 없습니다. {date: %s, timeId: %d, themeId: %d}",
                    date.getDate(), timeId, themeId));
        }
    }

    public List<WaitingWithRankAppResponse> findWaitingWithRankByMemberId(Long memberId) {
        return waitingRepository.findAllWaitingWithRankByMemberId(memberId)
                .stream()
                .map(WaitingWithRankAppResponse::new)
                .toList();
    }

    public void deleteMemberWaiting(Long memberId, Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("예약 대기 삭제 실패: 대기를 찾을 수 없습니다. (id: %d)", waitingId)));
        if (!waiting.hasMemberId(memberId)) {
            throw new AuthorizationException(String.format("예약 대기 삭제 권한이 없는 사용자입니다. (id: %d)", memberId));
        }
        waitingRepository.deleteById(waitingId);
    }
}
