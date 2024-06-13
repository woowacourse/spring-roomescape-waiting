package roomescape.reservation.service;

import static roomescape.member.model.MemberRole.USER;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import roomescape.auth.principal.AuthenticatedMember;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.SaveWaitingRequest;
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

    public Waiting saveWaiting(SaveWaitingRequest request, Long memberId) {
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

    public void deleteWaiting(Long waitingId, AuthenticatedMember member) {
        Long waitingMemberId = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 대기는 존재하지 않습니다."))
                .getMember()
                .getId();

        if (member.role() == USER && !waitingMemberId.equals(member.id())) {
            throw new IllegalArgumentException("자신의 예약 대기만 제거 할 수 있습니다");
        }
        waitingRepository.deleteById(waitingId);
    }

    public List<Waiting> getWaitings() {
        return waitingRepository.findAll();
    }

}
