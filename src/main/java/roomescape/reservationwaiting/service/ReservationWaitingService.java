package roomescape.reservationwaiting.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;

@Service
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
        if (reservationWaitingRepository.existsByNameAndSlot(request.name(), reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAITING);
        }
        ReservationWaiting reservationWaiting = reservationWaitingRepository.save(
                reservationWaitingFactory.create(request.name(), reservation));
        return ReservationWaitingResponse.from(reservationWaiting);
    }

    @Transactional
    public void deleteWaiting(Long id) {
        ReservationWaiting reservationWaiting = getById(id);
        if (reservationWaiting.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_WAITING_CANCEL);
        }
        reservationWaitingRepository.deleteById(id);
    }

    @Transactional
    public List<ReservationWaitingTurnResponse> getWaitingByName(String name) {
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.findByName(name);
        Map<Long, Long> turns = reservationWaitingRepository.calculateTurn(name);

        return reservationWaitings.stream()
                .map(waiting -> ReservationWaitingTurnResponse.from(waiting, turns.get(waiting.getId())))
                .toList();
    }

    @NonNull
    private ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findReservationWaitingById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_NOT_FOUND));
    }
}
