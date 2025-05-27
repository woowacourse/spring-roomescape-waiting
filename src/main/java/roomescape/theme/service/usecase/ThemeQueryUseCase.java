package roomescape.theme.service.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeQueryUseCase {

    private final ThemeRepository themeRepository;

    public List<Theme> getAll() {
        return themeRepository.findAll();
    }

    public Theme get(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow();
    }

    public List<Theme> getRanking(final ReservationDate startDate,
                                  final ReservationDate endDate,
                                  final Pageable pageable) {
        return themeRepository.getRanking(startDate, endDate, pageable).stream()
                .toList();
    }

}
