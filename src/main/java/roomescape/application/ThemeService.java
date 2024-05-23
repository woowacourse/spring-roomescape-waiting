package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.ThemeRequest;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.exception.BadRequestException;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toTheme();

        if (themeRepository.existsByName(theme.getName())) {
            String message = String.format("해당 이름의 테마는 이미 존재합니다. (이름: %s)", theme.getName());

            throw new BadRequestException(message);
        }

        Theme savedTheme = themeRepository.save(theme);

        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> getPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        List<Theme> themes = themeRepository.findPopularThemes(startDate, endDate, limit);

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteThemeById(Long id) {
        if (!themeRepository.existsById(id)) {
            throw new DomainNotFoundException(String.format("해당 id의 테마가 존재하지 않습니다. (id: %d)", id));
        }

        if (reservationRepository.existsByDetail_ThemeId(id)) {
            throw new BadRequestException("해당 테마를 사용하는 예약이 존재합니다.");
        }

        themeRepository.deleteById(id);
    }
}
