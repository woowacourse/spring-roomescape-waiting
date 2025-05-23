package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.util.TokenProvider;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationChecker reservationChecker;
    private final TokenProvider tokenProvider;

    public WaitingService(WaitingRepository waitingRepository, ReservationChecker reservationChecker, TokenProvider tokenProvider) {
        this.waitingRepository = waitingRepository;
        this.reservationChecker = reservationChecker;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public WaitingResponse createWaiting(WaitingRequest dto, Member member) {
        Waiting waiting = reservationChecker.createWaitingWithoutId(dto, member);
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId(), member.getId())) {
            throw new DuplicateContentException("[ERROR] 해당 날짜와 테마로 이미 예약대기 내역이 존재합니다.");
        }
        Waiting newWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(newWaiting, newWaiting.getTime(), newWaiting.getTheme());
    }

    public List<MemberReservationResponse> findAllMemberWaitings(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(memberId);
        return waitingWithRanks.stream().map(MemberReservationResponse::from).toList();
    }

    @Transactional
    public void deleteWaiting(Long id, Long memberId) {
        if (waitingRepository.findByIdAndMemberId(id, memberId).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약대기만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        waitingRepository.deleteById(id);
    }
}
