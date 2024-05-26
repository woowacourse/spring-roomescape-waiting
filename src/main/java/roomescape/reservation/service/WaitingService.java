package roomescape.reservation.service;

import org.springframework.stereotype.Service;

import roomescape.member.model.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public Waiting saveWaiting(WaitingRequest request, Long memberId) {
        Reservation reserved = reservationRepository.findByDateAndThemeIdAndTimeId(
                new ReservationDate(request.date()),
                request.themeId(),
                request.timeId());
        validateWaiting(memberId, reserved);
        Member member = memberRepository.findById(memberId).get();
        return waitingRepository.save(new Waiting(reserved, member));
    }

    private void validateWaiting(Long memberId, Reservation reserved) {
        if (memberId.equals(reserved.getMember().getId())) {
            throw new IllegalArgumentException("이미 예약이 확정되었습니다.");
        }
        if (waitingRepository.existsByMemberIdAndReservationId(memberId, reserved.getId())) {
            throw new IllegalArgumentException("예약 대기는 한번만 가능합니다");
        }
    }

    public void deleteWaiting(Long waitingId) {
        waitingRepository.deleteById(waitingId);
    }
}
