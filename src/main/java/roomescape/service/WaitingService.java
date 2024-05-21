package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

import java.util.List;

@Service
@Transactional
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final ReservationRepository reservationRepository,
                          final WaitingRepository waitingRepository,
                          final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(final Reservation reservation) {
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    public List<MyReservationResponse> findMyWaitings(Long id) {
        final List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(id);
        return waitings.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public ReservationResponse checkOwn(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(memberId + "에 해당하는 대기가 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(memberId + "에 해당하는 사용자가 없습니다."));

        if (reservation.isNotReservedBy(member)) {
            throw new IllegalArgumentException("예약자가 일치하지 않습니다.");
        }

        return ReservationResponse.from(reservation);
    }

    public void cancel(Long id) {
        reservationRepository.deleteById(id);
    }
}
