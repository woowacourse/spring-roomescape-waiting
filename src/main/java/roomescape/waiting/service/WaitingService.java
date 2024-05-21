package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.dto.request.CreateWaitingRequest;
import roomescape.waiting.dto.response.CreateWaitingResponse;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    // TODO: authInfo -> Member로 변경하기

    public WaitingService(final WaitingRepository waitingRepository,
                          final ReservationRepository reservationRepository, final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }


    public CreateWaitingResponse createWaiting(final AuthInfo authInfo,
                                               final CreateWaitingRequest createWaitingRequest) {
        Reservation reservation = reservationRepository.findByDateAndReservationTimeIdAndThemeId(
                        createWaitingRequest.date(), createWaitingRequest.timeId(), createWaitingRequest.themeId())
                .orElseThrow(() -> new IllegalCallerException("예약이 존재하지 않아서 대기가 불가능합니다. " + createWaitingRequest));
        Member member = memberRepository.findById(authInfo.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 회원이 존재하지 않습니다."));

        checkAlreadyExistsWaiting(authInfo.getMemberId(), reservation.getId());
        return CreateWaitingResponse.of(waitingRepository.save(new Waiting(reservation, member)));
    }

    private void checkAlreadyExistsWaiting(Long memberId, Long reservationId) {
        if (waitingRepository.existsByMemberIdAndReservationId(memberId, reservationId)) {
            throw new IllegalArgumentException(
                    "memberId: " + memberId + " 회원이 reservationId: " + reservationId + "인 예약에 대해 이미 대기를 신청했습니다.");
        }
    }
}
