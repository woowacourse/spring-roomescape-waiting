package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;

@Service
@Transactional
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final ReservationRepository reservationRepository, final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse create(final Reservation reservation) {
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
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
