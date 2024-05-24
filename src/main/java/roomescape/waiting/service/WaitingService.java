package roomescape.waiting.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.common.exception.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.dto.request.CreateWaitingRequest;
import roomescape.waiting.dto.response.CreateWaitingResponse;
import roomescape.waiting.dto.response.FindWaitingResponse;
import roomescape.waiting.dto.response.FindWaitingWithRankingResponse;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final WaitingRepository waitingRepository,
                          final ReservationRepository reservationRepository, final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public CreateWaitingResponse createWaiting(final AuthInfo authInfo,
                                               final CreateWaitingRequest createWaitingRequest) {
        Reservation reservation = reservationRepository.getByDateAndReservationTimeIdAndThemeId(
                createWaitingRequest.date(), createWaitingRequest.timeId(), createWaitingRequest.themeId());
        Member member = memberRepository.getById(authInfo.getMemberId());

        checkAlreadyExistsWaiting(authInfo.getMemberId(), reservation.getId());
        return CreateWaitingResponse.of(waitingRepository.save(new Waiting(reservation, member)));
    }

    private void checkAlreadyExistsWaiting(Long memberId, Long reservationId) {
        if (waitingRepository.existsByMemberIdAndReservationId(memberId, reservationId)) {
            throw new IllegalArgumentException(
                    "memberId: " + memberId + " 회원이 reservationId: " + reservationId + "인 예약에 대해 이미 대기를 신청했습니다.");
        }
    }

    public List<FindWaitingWithRankingResponse> getWaitingsByMember(final AuthInfo authInfo) {
        Member member = memberRepository.getById(authInfo.getMemberId());
        return waitingRepository.findWaitingsWithRankByMember(member)
                .stream()
                .map(FindWaitingWithRankingResponse::of)
                .toList();
    }

    public List<FindWaitingResponse> getWaitings() {
        return waitingRepository.findAll()
                .stream()
                .map(FindWaitingResponse::from)
                .toList();
    }

    public void deleteWaiting(final Long waitingId) {
        Waiting waiting = waitingRepository.getById(waitingId);
        waitingRepository.delete(waiting);
    }

    public void deleteWaiting(final AuthInfo authInfo, final Long waitingId) {
        Waiting waiting = waitingRepository.getById(waitingId);

        checkMemberAuthentication(waiting, authInfo.getMemberId());
        waitingRepository.delete(waiting);
    }

    private void checkMemberAuthentication(final Waiting waiting, final Long memberId) {
        if (waiting.isNotSameMember(memberId)) {
            throw new ForbiddenException("회원의 권한이 없어, 식별자 " + memberId + "인 예약 대기를 삭제할 수 없습니다.");
        }
    }

    public void rejectWaiting(final Long waitingId) {
        waitingRepository.findById(waitingId)
                .ifPresentOrElse(waitingRepository::delete, () -> {
                    throw new NoSuchElementException("식별자 " + waitingId + "인 예약 대기가 존재하지 않아 거절할 수 없습니다.");
                });
    }
}
