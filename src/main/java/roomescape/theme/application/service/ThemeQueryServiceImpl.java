package roomescape.theme.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.application.dto.ThemeToBookCountServiceResponse;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeQueryServiceImpl implements ThemeQueryService {

    private final ThemeRepository themeRepository;
    private final ReservationQueryService reservationQueryService;

    @Override
    public boolean existsById(final ThemeId id) {
        return themeRepository.existsById(id);
    }

    @Override
    public boolean existsByName(final ThemeName name) {
        return themeRepository.existsByName(name);
    }

    @Override
    public List<Theme> getAll() {
        return themeRepository.findAll();
    }

    @Override
    public Theme get(final ThemeId id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DomainTerm.THEME, id));
    }

    @Override
    public List<Theme> getRanking(final ReservationDate startDate, final ReservationDate endDate, final int count) {
        return reservationQueryService.getRanking(startDate, endDate, count).stream()
                .map(ThemeToBookCountServiceResponse::theme)
                .toList();
    }

}
