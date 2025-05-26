package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.NotFoundException;
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

    @Override
    public Long findUserIdById(final Long id) {
        return waitingReservationRepository.findUserIdById(id).orElseThrow(
                () -> new NotFoundException(DomainTerm.USER_ID, id)
        );
    }
}
