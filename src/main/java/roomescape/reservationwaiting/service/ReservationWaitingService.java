package roomescape.reservationwaiting.service;

import java.time.Clock;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingFactory reservationWaitingFactory;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     ReservationWaitingFactory reservationWaitingFactory,
                                     Clock clock) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingFactory = reservationWaitingFactory;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaitingResponse createWaiting(ReservationWaitingRequest request) {
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESERVATION_NOT_FOUND));
        if (reservationWaitingRepository.existsByNameAndReservationId(request.name(), reservation.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAITING);
        }
        ReservationWaiting reservationWaiting = reservationWaitingRepository.save(
                reservationWaitingFactory.create(request.name(), reservation));
        return ReservationWaitingResponse.from(reservationWaiting);
    }

    @Transactional
    public void deleteWaiting(Long id) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findReservationWaitingById(id);
        Reservation reservation = reservationWaiting.getReservation();
        if (reservation.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_WAITING_CANCEL);
        }
        reservationWaitingRepository.deleteById(id);
    }

    public List<ReservationWaitingTurnResponse> getWaitingByName(String name) {
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.findByName(name);
        List<Long> turns = reservationWaitingRepository.calculateTurn(name);

        return IntStream.range(0, turns.size())
                .mapToObj(i -> ReservationWaitingTurnResponse.from(reservationWaitings.get(i), turns.get(i)))
                .toList();
    }
}
