package roomescape.application;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.reservation.ReservationRequest;
import roomescape.application.dto.request.reservation.ReservationSearchCondition;
import roomescape.application.dto.response.reservation.ReservationResponse;
import roomescape.application.dto.response.reservation.UserReservationResponse;
import roomescape.domain.dto.ReservationWithRank;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationFactory;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Status;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.domain.reservationdetail.ReservationDetailFactory;
import roomescape.exception.reservation.ReservationException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationFactory reservationFactory;
    private final ReservationDetailFactory reservationDetailFactory;

    @Transactional
    public Reservation saveReservation(ReservationRequest request) {
        Member member = memberRepository.getById(request.memberId());
        ReservationDetail reservationDetail = reservationDetailFactory.createReservationDetail(
                request.date(), request.timeId(), request.themeId());
        Reservation reservation = reservationFactory.createReservation(reservationDetail, member);
        return reservationRepository.save(reservation);
    }

    public List<ReservationResponse> findAllReservationsWithoutCancel() {
        List<Reservation> reservations = reservationRepository.findAllByStatus(Status.RESERVED);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findByPeriodAndThemeAndMember(
                condition.start(), condition.end(), condition.themeId(), condition.memberId());
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<UserReservationResponse> findAllWithRank(Long memberId) {
        List<ReservationWithRank> reservationWithRanks = reservationRepository.findWithRank(memberId);

        List<UserReservationResponse> reservations = reservationWithRanks.stream()
                .filter(ReservationWithRank::isReserved)
                .map(ReservationWithRank::reservation)
                .map(UserReservationResponse::from)
                .toList();

        List<UserReservationResponse> waitings = reservationWithRanks.stream()
                .filter(ReservationWithRank::isWaiting)
                .map(UserReservationResponse::from)
                .toList();

        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }

    public List<ReservationResponse> findAllWaitings() {
        List<Reservation> reservations = reservationRepository.findAllByStatus(Status.WAITING);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public Reservation approveWaiting(Long waitingId) {
        Reservation waiting = reservationRepository.getById(waitingId);
        rejectIfAnyReservationExist(waiting);
        return waiting.approve();
    }

    private void rejectIfAnyReservationExist(Reservation reservation) {
        boolean reservationExists = reservationRepository.existsByDetailAndStatus(
                reservation.getDetail(), Status.RESERVED);
        if (reservationExists) {
            throw new ReservationException("다른 예약이 존재합니다.");
        }
    }
}
