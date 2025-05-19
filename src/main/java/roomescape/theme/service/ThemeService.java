package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.DataNotFoundException;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

@RequiredArgsConstructor
@Service
public class ThemeService {

    private static final int POPULAR_THEME_DAYS = 7;
    private static final int POPULAR_THEME_LIMIT = 10;

    private final ThemeRepositoryInterface themeRepository;
    private final ReservationRepositoryInterface reservationRepository;

    public Theme save(final String name, final String description, final String thumbnail) {
        if (themeRepository.existsByName(name)) {
            throw new DataExistException("해당 테마명이 이미 존재합니다. name = " + name);
        }

        final Theme theme = new Theme(name, description, thumbnail);

        return themeRepository.save(theme);
    }

    public void deleteById(final Long id) {
        themeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + id));

        themeRepository.deleteById(id);
    }

    public Theme getById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + id));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findPopularThemes() {
        final LocalDate sevenDaysAgo = LocalDate.now().minusDays(POPULAR_THEME_DAYS);
        final LocalDate today = LocalDate.now();
        final PageRequest pageRequest = PageRequest.of(0, POPULAR_THEME_LIMIT);

        return reservationRepository.findPopularThemesByReservationBetween(
                sevenDaysAgo,
                today,
                pageRequest
        );
    }
}
