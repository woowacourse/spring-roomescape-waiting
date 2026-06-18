package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final JpaThemeRepository jpaThemeRepository;
    private final JpaReservationRepository jpaReservationRepository;

    public ThemeService(
            final JpaThemeRepository jpaThemeRepository,
            final JpaReservationRepository jpaReservationRepository
    ) {
        this.jpaThemeRepository = jpaThemeRepository;
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Transactional
    public Theme save(final String name, final String description, final String thumbnailUrl) {
        Theme nonIdTheme;
        try {
            nonIdTheme = Theme.createNew(name, description, thumbnailUrl);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
        }

        if (jpaThemeRepository.existsByName(name)) {
            throw new ConflictException(ErrorCode.THEME_NAME_DUPLICATED, "테마 이름 중복은 불가능합니다.");
        }

        return jpaThemeRepository.save(nonIdTheme);
    }

    public List<Theme> getAll() {
        return jpaThemeRepository.findAll();
    }

    public Theme getById(final long themeId) {
        return jpaThemeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND, "테마를 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteById(final long themeId) {
        if (!jpaThemeRepository.existsById(themeId)) {
            throw new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND, "존재하지 않는 테마입니다.");
        }
        if (jpaReservationRepository.existsByThemeId(themeId)) {
            throw new ConflictException(ErrorCode.THEME_IN_USE, "이미 예약된 테마는 삭제할 수 없습니다.");
        }

        jpaThemeRepository.deleteById(themeId);
    }

    public List<Theme> getPopularThemes(final int period, final int limit) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(period);
        return jpaThemeRepository.findPopularThemes(start, end, limit);
    }
}
