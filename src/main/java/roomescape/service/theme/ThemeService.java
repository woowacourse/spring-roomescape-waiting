package roomescape.service.theme;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeSaveRequest;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {

    private static final int START_DAYS_SUBTRACT = 7;
    private static final int END_DAYS_SUBTRACT = 1;
    private static final int RANK_COUNT = 7;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository,
                        ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Theme createTheme(ThemeSaveRequest request) {
        Theme theme = request.toEntity(request);
        return themeRepository.save(theme);
    }

    public List<Theme> findThemes() {
        return themeRepository.findAll();
    }

    public List<Theme> findThemeRanks() {
        return themeRepository.findPopularThemes(
                LocalDate.now().minusDays(START_DAYS_SUBTRACT),
                LocalDate.now().minusDays(END_DAYS_SUBTRACT),
                PageRequest.of(0, RANK_COUNT)
        );
    }

    public void deleteTheme(long id) {
        themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마 아이디 입니다."));

        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("이미 예약중인 테마는 삭제할 수 없습니다.");
        }

        themeRepository.deleteById(id);
    }
}
