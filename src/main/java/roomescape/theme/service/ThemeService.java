package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.error.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.PopularThemeResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeResponse saveTheme(final ThemeRequest request) {
        final Theme theme = themeRepository.save(new Theme(request.name(), request.description(), request.thumbnail()));
        return new ThemeResponse(theme);
    }

    public List<ThemeResponse> findAll() {
        final List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<PopularThemeResponse> findAllPopular() {
        final LocalDate startDate = java.time.LocalDate.now().minusDays(7);
        final LocalDate endDate = LocalDate.now().minusDays(1);
        return themeRepository.findAllPopular(startDate, endDate)
                .stream()
                .map(PopularThemeResponse::new)
                .toList();
    }

    public void delete(final Long id) {
        if (reservationRepository.existsByThemeId((id))) {
            throw new ReservationException("해당 테마로 예약된 건이 존재합니다.");
        }

        /*final int deletedCount = */
        themeRepository.deleteById(id);
//        if (deletedCount == 0) {
//            throw new NotFoundException("존재하지 않는 테마입니다. id=" + id);
//        }
    }
}

