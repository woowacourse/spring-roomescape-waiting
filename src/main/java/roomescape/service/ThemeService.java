package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_THEMES_COUNT = 10;

    private final JpaThemeRepository themeRepository;
    private final JpaReservationRepository reservationRepository;

    public ThemeService(final JpaThemeRepository themeRepository,
                        final JpaReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponseDto createTheme(final ThemeCreateRequestDto requestDto) {
        Theme requestTheme = requestDto.createWithoutId();
        try {
            Theme savedTheme = themeRepository.save(requestTheme);
            return ThemeResponseDto.from(savedTheme);
        } catch (IllegalStateException e) {
            throw new DuplicateContentException(e.getMessage());
        }
    }

    public List<ThemeResponseDto> findAllThemes() {
        List<Theme> allTheme = themeRepository.findAll();
        return allTheme.stream()
                .map(ThemeResponseDto::from)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalStateException("[ERROR] 이 테마는 이미 예약이 존재합니다. id : " + id);
        }

        if (!themeRepository.existsById(id)) {
            throw new NotFoundException("[ERROR] 등록된 테마만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        themeRepository.deleteById(id);
    }

    public List<ThemeResponseDto> findPopularThemes() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        List<Theme> popularThemes = themeRepository.findMostReservedThemesBetween(start, end)
                .subList(0, POPULAR_THEMES_COUNT);
        return popularThemes.stream()
                .map(ThemeResponseDto::from)
                .toList();
    }
}
