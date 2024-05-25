package roomescape.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationDetailRepository detailRepository;

    public ReservationWaitingService(ReservationWaitingRepository waitingRepository,
                                     MemberRepository memberRepository,
                                     ReservationDetailRepository detailRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.detailRepository = detailRepository;
    }

    public List<MyReservationResponse> findReservationByMemberId(Long id) {
        List<ReservationWaiting> reservationsByMember
                = waitingRepository.findAllByMember_IdOrderByDetailDateAsc(id);
        return reservationsByMember.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
