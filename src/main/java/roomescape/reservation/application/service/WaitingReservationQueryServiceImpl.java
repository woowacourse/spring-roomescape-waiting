package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingReservationQueryServiceImpl implements WaitingReservationQueryService {

    private final WaitingReservationRepository waitingReservationRepository;

    @Override
    public List<WaitingReservation> getAll() {
        return waitingReservationRepository.findAll();
    }
}
