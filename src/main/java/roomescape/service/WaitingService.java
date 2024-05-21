package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final WaitingRepository waitingRepository,
                          final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(final Waiting waiting) {
        validateDate(waiting.getDate());
        Waiting saved = waitingRepository.save(waiting);
        return ReservationResponse.from(saved);
    }

    private void validateDate(final LocalDate date) {
        if (LocalDate.now().isAfter(date) || LocalDate.now().equals(date)) {
            throw new IllegalArgumentException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    public List<MyReservationResponse> findMyWaitings(Long id) {
        final List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(id);
        return waitings.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public void cancel(Long memberId, Long reservationId) {
        Waiting waiting = waitingRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(memberId + "에 해당하는 대기가 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(memberId + "에 해당하는 사용자가 없습니다."));

        if (waiting.isNotReservedBy(member)) {
            throw new IllegalArgumentException("예약자가 일치하지 않습니다.");
        }
        waitingRepository.deleteById(reservationId);
    }
}
