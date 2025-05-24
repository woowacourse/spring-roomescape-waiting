package roomescape.reservation.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.AuthorizationException;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.application.dto.response.UserReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;
import roomescape.reservation.model.service.ReservationOperation;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationOperation reservationOperation;

    @Transactional
    public ReservationServiceResponse create(CreateReservationServiceRequest request) {
        Reservation savedReservation = reservationOperation.reserve(request.date(), request.timeId(),
                request.themeId(), request.memberId());
        return ReservationServiceResponse.from(savedReservation);
    }

    public List<UserReservationServiceResponse> getAllByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        List<ReservationWaitingWithRank> reservationWaitingWithRanks = reservationWaitingRepository.findAllWithRankByMemberId(
                memberId);

        List<UserReservationServiceResponse> responses = createUserReservationServiceResponse(
                reservations,
                reservationWaitingWithRanks
        );

        return sortByDateTime(responses);
    }

    @Transactional
    public void cancel(Long id, Long memberId) {
        Reservation reservation = reservationRepository.getById(id);
        if (reservation.hasNotEqualsMemberId(memberId)) {
            throw new AuthorizationException("해당 예약을 취소할 권한이 없습니다.");
        }
        reservationOperation.cancel(reservation);
    }

    private List<UserReservationServiceResponse> createUserReservationServiceResponse(
            List<Reservation> reservations,
            List<ReservationWaitingWithRank> reservationWaitingWithRanks
    ) {
        List<UserReservationServiceResponse> responses = new ArrayList<>();
        for (Reservation reservation : reservations) {
            responses.add(UserReservationServiceResponse.of(reservation));
        }
        for (ReservationWaitingWithRank waitingWithRank : reservationWaitingWithRanks) {
            ReservationWaiting reservationWaiting = waitingWithRank.getReservationWaiting();
            int rank = waitingWithRank.getRankToInt();
            responses.add(UserReservationServiceResponse.of(reservationWaiting, rank));
        }

        return responses;
    }

    private List<UserReservationServiceResponse> sortByDateTime(List<UserReservationServiceResponse> responses) {
        return responses.stream()
                .sorted(Comparator.comparing(UserReservationServiceResponse::date)
                        .thenComparing(UserReservationServiceResponse::time))
                .toList();
    }
}
