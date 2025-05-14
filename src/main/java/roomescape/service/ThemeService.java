package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ConstrainedDataException;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    public static final int POPULAR_THEME_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository1) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository1;
    }

    public ThemeResponse createTheme(final ThemeCreateRequest requestDto) {
        Theme requestTheme = requestDto.createWithoutId();
        try {
            Theme savedTheme = themeRepository.save(requestTheme);
            return ThemeResponse.from(savedTheme);
        } catch (DuplicateKeyException e) {
            throw new DuplicateContentException("[ERROR] 이미 동일한 이름의 테마가 존재합니다.");
        }
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

        List<Theme> themes = themeRepository.findTopThemes(start, end, PageRequest.of(0, POPULAR_THEME_LIMIT));
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
