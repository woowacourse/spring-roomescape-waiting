package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.repository.ReservationQueryRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationQueryService {

    private final ReservationQueryRepository queryRepository;

    public boolean existsByTimeId(Long timeId) {
        return queryRepository.existsByTimeId(timeId);
    }

    public boolean existsByThemeId(Long themeId) {
        return queryRepository.existsByThemeId(themeId);
    }
}
