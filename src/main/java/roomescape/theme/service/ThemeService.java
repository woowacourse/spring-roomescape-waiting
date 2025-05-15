package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.exception.ConstrainedDataException;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    public static final int POPULAR_THEME_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final ThemeCreateRequest requestDto) {
        if (themeRepository.existsByName(requestDto.name())) {
            throw new DuplicateContentException("[ERROR] 이미 동일한 이름의 테마가 존재합니다.");
        }
        Theme requestTheme = requestDto.createWithoutId();
        Theme savedTheme = themeRepository.save(requestTheme);
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> allTheme = themeRepository.findAll();
        return allTheme.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 테마만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        if (reservationRepository.findByThemeId(id).isPresent()) {
            throw new ConstrainedDataException("[ERROR] 해당 테마에 예약 기록이 존재합니다. 예약을 먼저 삭제해 주세요.");
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findPopularThemes() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        List<Theme> themes = themeRepository.findTopThemes(start, end, POPULAR_THEME_LIMIT);
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
