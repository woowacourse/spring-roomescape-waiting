package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.ReservationView;
import roomescape.reservation.domain.ReservationViewRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationViewQueryServiceImpl implements ReservationViewQueryService {

    private final ReservationViewRepository viewRepository;

    @Override
    public List<ReservationView> getAllByUserId(final Long userId) {
        return viewRepository.findAllByUserId(userId);
    }

    @Override
    public boolean existsByParams(final CreateReservationServiceRequest request, final Long userId) {
        return viewRepository.existsByParams(request.date(), request.timeId(), request.themeId(), userId);
    }

    @Override
    public Optional<Long> findFirstWaitingByReservationId(final Long id) {
        return viewRepository.findFirstWaitingByReservationId(id);
    }
}
