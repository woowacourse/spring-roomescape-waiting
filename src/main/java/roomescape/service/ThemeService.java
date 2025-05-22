package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final ThemeCreateRequest requestDto) {
        Theme requestTheme = requestDto.createWithoutId();
        try {
            Theme savedTheme = themeRepository.save(requestTheme);
            return ThemeResponse.from(savedTheme);
        } catch (IllegalStateException e) {
            throw new DuplicateContentException(e.getMessage());
        }
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> allTheme = themeRepository.findAll();
        return allTheme.stream()
                .map(ThemeResponse::from)
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

    public List<ThemeResponse> findPopularThemes() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        List<Theme> popularThemes = themeRepository.findPopular(start, end);
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
