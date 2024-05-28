package roomescape.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse save(ThemeRequest themeRequest) {
        validateDuplicateTheme(themeRequest);
        Theme saved = themeRepository.save(
                new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail()));

        return ThemeResponse.from(saved);
    }

    private void validateDuplicateTheme(ThemeRequest themeRequest) {
        boolean hasDuplicateTheme = themeRepository.findAll()
                .stream()
                .anyMatch(theme -> theme.isNameOf(themeRequest.name()));

        if (hasDuplicateTheme) {
            throw new RoomescapeException(DUPLICATE_THEME);
        }
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findAndOrderByPopularity(LocalDate start, LocalDate end, int count) {
        return themeRepository.findAndOrderByPopularityFirstTheme(start, end, PageRequest.of(0, count))
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(long id) {
        validateUsedTheme(id);
        themeRepository.deleteById(id);
    }

    private void validateUsedTheme(long id) {
        themeRepository.findById(id)
                .ifPresent(this::validateUsedTheme);
    }

    private void validateUsedTheme(Theme theme) {
        boolean existsByTime = reservationRepository.existsByTheme(theme);
        if (existsByTime) {
            throw new RoomescapeException(DELETE_USED_THEME);
        }
    }
}
