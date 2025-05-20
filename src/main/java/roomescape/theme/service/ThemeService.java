package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService implements ThemeQueryService {

    private static final int POPULAR_THEME_LIMIT = 10;
    private static final int POPULAR_THEME_EXPIRES_DAYS = 7;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void deleteById(Long id) {
        if (reservationRepository.existsByTheme_Id(id)) {
            throw new InvalidArgumentException("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
        Theme theme = getTheme(id);
        themeRepository.deleteById(theme.getId());
    }

    @Transactional
    public ThemeResponse create(ThemeCreateRequest request) {
        Theme created = Theme.create(request.name(), request.description(), request.thumbnail());
        Theme saved = themeRepository.save(created);
        return ThemeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ThemeResponse> getAll() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    @Transactional(readOnly = true)
    @Override
    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 테마가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ThemeResponse> getPopularThemes() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(POPULAR_THEME_EXPIRES_DAYS);

        return themeRepository.findAll().stream()
                .sorted((t1, t2) -> Long.compare(
                        reservationRepository.countReservationByThemeIdAndDuration(from, to, t1.getId()),
                        reservationRepository.countReservationByThemeIdAndDuration(from, to, t2.getId())
                ))
                .limit(POPULAR_THEME_LIMIT)
                .map(ThemeResponse::from)
                .toList();
    }
}
