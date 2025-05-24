package roomescape.waiting.application;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservation;
import roomescape.waiting.application.dto.WaitingIdResponse;
import roomescape.reservation.domain.ReservationTime;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingWithRank;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.waiting.application.dto.WaitingInfoResponse;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
        final WaitingRepository waitingRepository,
        final ReservationRepository reservationRepository,
        final ReservationTimeRepository reservationTimeRepository,
        final ThemeRepository themeRepository,
        final MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WaitingIdResponse addWaiting(@Valid MemberReservationRequest request, Long memberId) {
        ReservationTime reservationTime = getReservationTime(request.timeId());
        Member member = getMember(memberId);
        Theme theme = getTheme(request.themeId());
        Waiting waiting = new Waiting(member, reservationTime, theme, request.date());
        validateAlreadyReserved(request, member, theme, reservationTime);
        validateAlreadyWaiting(request, member, theme, reservationTime);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingIdResponse.from(savedWaiting);
    }

    private void validateAlreadyReserved(
        MemberReservationRequest request, Member member, Theme theme, ReservationTime reservationTime) {
        boolean isAlreadyReserved = reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(
            member.getId(), theme.getId(), reservationTime.getId(), request.date());
        if (isAlreadyReserved) {
            throw new IllegalArgumentException("이미 예약이 되어있는 상태에서는, 대기할 수 없습니다.");
        }
    }

    private void validateAlreadyWaiting(
        MemberReservationRequest request,
        Member member,
        Theme theme,
        ReservationTime reservationTime
    ) {
        boolean isAlreadyWaiting = waitingRepository.existsByMemberIdAndThemeIdAndReservationTimeIdAndDate(
            member.getId(), theme.getId(), reservationTime.getId(), request.date());
        if (isAlreadyWaiting) {
            throw new IllegalArgumentException("이미 대기중인 상태에서는, 추가로 대기할 수 없습니다.");
        }
    }

    public List<MyReservation> getWaitingsFromMember(Long memberId) {
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(
            memberId);
        return waitingWithRanks.stream()
            .map(MyReservation::from)
            .toList();
    }

    @Transactional
    public void cancel(Long memberId, Long waitingId) {
        Waiting waiting = getWaiting(waitingId);
        if (!waiting.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 대기만 삭제할 수 있습니다.");
        }
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void cancelFromAdmin(Long waitingId) {
        getWaiting(waitingId);
        waitingRepository.deleteById(waitingId);
    }

    public List<WaitingInfoResponse> getAllWaitingInfos() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
            .map(WaitingInfoResponse::from)
            .toList();
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다."));
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("선택한 멤버가 존재하지 않습니다."));
    }

    private Waiting getWaiting(final Long waitingId) {
        return waitingRepository.findById(waitingId)
            .orElseThrow(() -> new NotFoundException("선택한 웨이팅이 존재하지 않습니다."));
    }
}
