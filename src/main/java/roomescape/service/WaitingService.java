package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.domain.dto.WaitingRequest;
import roomescape.domain.dto.WaitingResponse;
import roomescape.exception.DeleteNotAllowException;
import roomescape.exception.ReservationFailException;
import roomescape.exception.clienterror.InvalidIdException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository, MemberRepository memberRepository,
                          ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<WaitingWithRank> findWaitingsByMember(final Member member) {
        return waitingRepository.findWaitingsWithRankByMemberId(member.getId());
    }

    @Transactional
    public WaitingResponse create(final WaitingRequest waitingRequest) {
        validateDuplicatedReservation(waitingRequest);
        validateDuplicatedWaiting(waitingRequest);
        final ReservationTime reservationTime = getReservationTime(waitingRequest);
        final Theme theme = getTheme(waitingRequest);
        final Member member = getMember(waitingRequest);
        final Waiting waiting = new Waiting(member, waitingRequest.date(), reservationTime, theme);
        waiting.validateDateTime();
        final Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private void validateDuplicatedReservation(final WaitingRequest waitingRequest) {
        if (reservationRepository.existsByDateAndTimeIdAndMemberId(waitingRequest.date(), waitingRequest.timeId(),
                waitingRequest.memberId())) {
            throw new ReservationFailException("이미 해당 시간에 예약된 내역이 있습니다..");
        }
    }

    private void validateDuplicatedWaiting(final WaitingRequest waitingRequest) {
        if (waitingRepository.existsByDateAndTimeIdAndMemberId(waitingRequest.date(), waitingRequest.timeId(),
                waitingRequest.memberId())) {
            throw new ReservationFailException("이미 해당 시간에 예약 대기중인 내역이 있습니다..");
        }
    }

    private ReservationTime getReservationTime(final WaitingRequest waitingRequest) {
        return reservationTimeRepository.findById(waitingRequest.timeId())
                .orElseThrow(() -> new InvalidIdException("timeId", waitingRequest.timeId()));
    }

    private Theme getTheme(final WaitingRequest reservationRequest) {
        return themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new InvalidIdException("themeId", reservationRequest.themeId()));
    }

    private Member getMember(final WaitingRequest reservationRequest) {
        return memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new InvalidIdException("memberId", reservationRequest.memberId()));
    }

    @Transactional
    public void delete(final Long waitingId, final Member member) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NoSuchElementException("해당되는 예약 대기 내역이 없습니다."));
        if (waiting.getMember() != member) {
            throw new DeleteNotAllowException("예약 대기는 본인만 취소할 수 있습니다.");
        }
        waitingRepository.deleteById(waitingId);
    }
}
