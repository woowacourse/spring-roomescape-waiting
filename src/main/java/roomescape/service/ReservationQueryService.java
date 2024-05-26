package roomescape.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
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
        Reservation reservation = reservationRepository.getById(id);
        return ReservationResponse.from(reservation);
    }

    public List<UserReservationResponse> getReservationByMemberId(Long memberId) {
        Member member = memberRepository.getById(memberId);
        List<UserReservationResponse> userWaitings = waitingRepository.findWaitingsWithSequenceByMember(member).stream()
                .map(UserReservationResponse::from)
                .toList();

        List<UserReservationResponse> userReservations = reservationRepository.findAllByMember(member).stream()
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
}
