package roomescape.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.WaitingWithSequence;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public ReservationQueryService(
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse getReservation(Long id) {
        Reservation reservation = findReservationById(id);
        return ReservationResponse.from(reservation);
    }

    public List<UserReservationResponse> getReservationByMemberId(Long memberId) {
        Member member = findMember(memberId);
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<WaitingWithSequence> waitings = waitingRepository.findWaitingsWithSequenceByMember(member);

        List<UserReservationResponse> userWaitings = waitings.stream()
                .map(UserReservationResponse::from)
                .toList();

        List<UserReservationResponse> userReservations = reservations.stream()
                .map(UserReservationResponse::from)
                .toList();

        List<UserReservationResponse> result = new ArrayList<>();
        result.addAll(userReservations);
        result.addAll(userWaitings);

        return result;
    }

    public List<ReservationResponse> getReservationsByFilter(ReservationFilter filter) {
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                filter.getMemberId(),
                filter.getThemeId(),
                filter.getDateFrom(),
                filter.getDateTo()
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 사용자 정보 입니다.",
                        new Throwable("member_id : " + memberId)
                ));
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + id)
                ));
    }
}
