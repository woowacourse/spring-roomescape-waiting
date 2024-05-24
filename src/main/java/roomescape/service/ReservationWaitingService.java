package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.dto.CreateReservationRequest;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository, MemberRepository memberRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse addReservationWaiting(CreateReservationRequest request) {
        Member member = getMember(request.memberId());
        //- 확정된 예약이 존재 → date, theme, time으로 예약 조회
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                        request.themeId())
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.findAllByReservation(reservation);
        //- 예약 대기 제한 개수 (대기 개수가 10개 등)
        if (reservationWaitings.size() >= 10) {
            throw new IllegalArgumentException("최대 예약 대기 10개");
        }

        //- 한 멤버는 한 예약에 대한 하나의 예약 대기만 가능
        reservationWaitings.stream()
                .filter(reservationWaiting -> reservationWaiting.isSameMember(member))
                .findAny()
                .ifPresent(reservationWaiting -> {
                    throw new IllegalArgumentException("이미 예약 대기 중입니다.");
                });

        ReservationWaiting reservationWaiting = reservationWaitingRepository.save(
                request.toReservationWaiting(reservation, member));

        return ReservationResponse.from(reservationWaiting);
    }

    private Member getMember(long request) {
        return memberRepository.findById(request)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    @Transactional
    public void deleteReservationWaiting(long waitingId, long memberId) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 대기입니다."));
        Member member = getMember(memberId);
        if (member.isNotAdmin()) {
            reservationWaiting.validateOwner(member);
        }
        reservationWaitingRepository.delete(reservationWaiting);
    }
}
