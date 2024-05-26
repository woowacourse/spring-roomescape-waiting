package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.dto.response.MyReservationResponse;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public MemberService(MemberRepository memberRepository, ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<MemberPreviewResponse> getAllMemberPreview() {
        return memberRepository.findAll().stream()
                .map(MemberPreviewResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(Member member) {
        return reservationRepository.findByMemberId(member.getId()).stream()
                .map(this::getMyReservationsWithWaitRank)
                .toList();
    }

    private MyReservationResponse getMyReservationsWithWaitRank(Reservation reservation) {
        long waitingRank = 0L;
        if (reservation.getReservationStatus().isWaiting()) {
            waitingRank = reservationRepository.countPreviousReservationsWithSameDateThemeTimeAndStatus(reservation.getId(), ReservationStatus.WAITING);
        }
        
        return MyReservationResponse.of(reservation, waitingRank);
    }
}
