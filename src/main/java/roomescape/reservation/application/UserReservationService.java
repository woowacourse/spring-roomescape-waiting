package roomescape.reservation.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.AuthorizationException;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.application.dto.response.UserReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.exception.ReservationException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;
import roomescape.reservation.model.service.ReservationOperation;
import roomescape.reservation.model.vo.ReservationStatus;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationOperation reservationOperation;

    @Transactional
    public ReservationServiceResponse create(CreateReservationServiceRequest request) {
        try {
            Reservation reservation = reservationOperation.reserve(request.date(), request.timeId(),
                    request.themeId(), request.memberId());
            Reservation savedReservation = reservationRepository.save(reservation);
            return ReservationServiceResponse.from(savedReservation);
        } catch (ReservationException e) {
            throw new BusinessRuleViolationException(e.getMessage(), e);
        }
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
    public void delete(Long id, Long memberId) {
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
            ReservationStatus reservationStatus = reservation.determineReservationStatus(LocalDateTime.now());
            responses.add(UserReservationServiceResponse.of(reservation, reservationStatus));
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
