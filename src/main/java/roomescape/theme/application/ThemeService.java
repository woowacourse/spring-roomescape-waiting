package roomescape.theme.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.theme.application.dto.ThemeRequest;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int TOP_THEME_COUNT = 10;

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(final ReservationRepository reservationRepository, final ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        final List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public ThemeResponse add(final ThemeRequest requestDto) {
        if (themeRepository.existsByName(requestDto.name())) {
            throw new BadRequestException("동일한 이름의 테마가 이미 존재합니다.");
        }
        final Theme theme = new Theme(requestDto.name(), requestDto.description(), requestDto.thumbnail());
        final Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    @Transactional
    public void deleteById(final Long id) {
        if (reservationRepository.existsByTheme_Id(id)) {
            throw new BadRequestException("이 테마의 예약이 존재합니다.");
        }
        themeRepository.deleteById(id);
    }

    @Transactional
    public List<ThemeResponse> sortByRank() {
        final List<Theme> themes = themeRepository.findByRank(TOP_THEME_COUNT);
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
