package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
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

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(final WaitingRepository waitingRepository,
                          final MemberRepository memberRepository,
                          final ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse create(final Waiting waiting) {
        validate(waiting);
        final Waiting saved = waitingRepository.save(waiting);
        return ReservationResponse.from(saved);
    }

    private void validate(final Waiting waiting) {
        if (!waiting.isAvailable()) {
            throw new IllegalArgumentException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }

        final boolean duplicated = waitingRepository.existsByDateAndTime_IdAndTheme_IdAndMember_Id(
                waiting.getDate(), waiting.getTimeId(), waiting.getThemeId(), waiting.getMemberId());
        if (duplicated) {
            throw new IllegalArgumentException("이미 예약 대기가 있습니다.");
        }
    }

    public List<MyReservationResponse> findMyWaitings(Long id) {
        final List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(id);
        return waitings.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public void cancel(Long memberId, Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException(memberId + "에 해당하는 대기가 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(memberId + "에 해당하는 사용자가 없습니다."));

        if (waiting.isNotReservedBy(member)) {
            throw new IllegalArgumentException("예약자가 일치하지 않습니다.");
        }
        waitingRepository.deleteById(waitingId);
    }

    public List<ReservationResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse approve(Long waitingId) {
        final Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대기입니다."));

        final Reservation reservation = waiting.toReservation();
        final Reservation saved = reservationRepository.save(reservation);
        waitingRepository.deleteById(waitingId);
        return ReservationResponse.from(saved);
    }

    public void deny(Long waitingId) {
        boolean exists = waitingRepository.existsById(waitingId);
        if (!exists) {
            throw new IllegalArgumentException("존재하지 않는 대기입니다.");
        }
        waitingRepository.deleteById(waitingId);
    }
}
