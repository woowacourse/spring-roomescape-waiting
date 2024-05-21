package roomescape.reservation.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.dto.request.CreateThemeRequest;
import roomescape.reservation.dto.response.CreateThemeResponse;
import roomescape.reservation.dto.response.FindPopularThemesResponse;
import roomescape.reservation.dto.response.FindThemeResponse;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public CreateThemeResponse createTheme(CreateThemeRequest createThemeRequest) {
        Theme theme = themeRepository.save(createThemeRequest.toTheme());
        return CreateThemeResponse.from(theme);
    }

    public List<FindThemeResponse> getThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(FindThemeResponse::from)
                .toList();
    }

    public List<FindPopularThemesResponse> getPopularThemes(Pageable pageable) {
        return themeRepository.findAllOrderByReservationCount(pageable).stream()
                .map(FindPopularThemesResponse::from)
                .toList();
    }

    public void deleteById(final Long id) {
        validateExistTheme(id);
        validateThemeUsage(id);

        themeRepository.deleteById(id);
    }

    private void validateExistTheme(final Long id) {
        if (!themeRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }

    private void validateThemeUsage(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalStateException("식별자 " + id + "인 테마를 사용 중인 예약이 존재합니다. 삭제가 불가능합니다.");
        }
    }
}
