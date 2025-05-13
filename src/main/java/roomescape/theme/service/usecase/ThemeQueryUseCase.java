package roomescape.theme.service.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.service.dto.ThemeToBookCountServiceResponse;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ThemeQueryUseCase {

    private final ThemeRepository themeRepository;
    private final ReservationQueryUseCase reservationQueryUseCase;

    public List<Theme> getAll() {
        return themeRepository.findAll();
    }

    public Theme get(final ThemeId id) {
        return themeRepository.findById(id)
                .orElseThrow();
    }

    public List<Theme> getRanking(final ReservationDate startDate, final ReservationDate endDate, final int count) {
        return reservationQueryUseCase.getRanking(startDate, endDate, count).stream()
                .map(ThemeToBookCountServiceResponse::theme)
                .toList();
    }

}
