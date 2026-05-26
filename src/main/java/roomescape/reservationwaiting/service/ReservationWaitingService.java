package roomescape.reservationwaiting.service;

import java.util.List;
import java.util.stream.Collectors;

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
import roomescape.reservationwaiting.repository.JdbcReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final JdbcReservationWaitingRepository jdbcReservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingFactory reservationWaitingFactory;

    public ReservationWaitingService(JdbcReservationWaitingRepository jdbcReservationWaitingRepository, ReservationRepository reservationRepository, ReservationWaitingFactory reservationWaitingFactory) {
        this.jdbcReservationWaitingRepository = jdbcReservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingFactory = reservationWaitingFactory;
    }

    @Transactional
    public ReservationWaitingResponse createWaiting(ReservationWaitingRequest request) {
        Reservation reservation = reservationRepository.findById(request.reservationId()).orElseThrow(() ->new BusinessException(
                ErrorCode.RESERVATION_NOT_FOUND));
        ReservationWaiting reservationWaiting = jdbcReservationWaitingRepository.save(reservationWaitingFactory.create(request.name(), reservation));
        return ReservationWaitingResponse.from(reservationWaiting);
    }

    @Transactional
    public void deleteWaiting(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationWaitingResponse> getWaitingByName(String name) {
        return jdbcReservationWaitingRepository.findByName(name).stream()
                .map(ReservationWaitingResponse::from)
                .collect(Collectors.toList());
    }
}
