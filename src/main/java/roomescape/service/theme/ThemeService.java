package roomescape.service.theme;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.theme.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(
            final ThemeRepository themeRepository,
            final ReservationRepository reservationRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Theme save(final String name, final String description, final String thumbnailUrl) {
        Theme nonIdTheme;
        try {
            nonIdTheme = Theme.createNew(name, description, thumbnailUrl);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
        }

        if (themeRepository.existsByName(name)) {
            throw new ConflictException(ErrorCode.THEME_NAME_DUPLICATED, "테마 이름 중복은 불가능합니다.");
        }

        try {
            return themeRepository.save(nonIdTheme);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(ErrorCode.THEME_NAME_DUPLICATED, "테마 이름 중복은 불가능합니다.");
        }
    }

    public List<Theme> getAll() {
        return themeRepository.findAll();
    }

    public Theme getById(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND, "테마를 찾을 수 없습니다."));
    }

    public void deleteById(final long themeId) {
        if (reservationRepository.findAll().stream()
                .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId))) {
            throw new ConflictException(ErrorCode.THEME_IN_USE, "이미 예약된 테마는 삭제할 수 없습니다.");
        }

        int affectedRowCount;
        try {
            affectedRowCount = themeRepository.deleteById(themeId);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(ErrorCode.THEME_IN_USE, "이미 예약된 테마는 삭제할 수 없습니다.");
        }

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND, "삭제된 테마 데이터가 없습니다.");
        }
    }

    public List<Theme> getPopularThemes(final int period, final int limit) {
        return themeRepository.findPopularThemes(period, limit);
    }
}
