package roomescape.theme.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequestDTO;
import roomescape.theme.dto.ThemeResponseDTO;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final Clock clock;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;


    public ThemeService(Clock clock, ThemeRepository themeRepository,
                        ReservationRepository reservationRepository) {
        this.clock = clock;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ThemeResponseDTO addTheme(ThemeRequestDTO request) {
        Theme theme = Theme.create(request.name(), request.description(), request.imageUrl());
        validateDuplicateTheme(theme);
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponseDTO.from(savedTheme);
    }

    private void validateDuplicateTheme(Theme theme) {
        if (themeRepository.existByThemeName(theme.getName())) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_DUPLICATE);
        }
    }

    @Transactional(readOnly = true)
    public ThemeResponseDTO findById(Long id) {
        Theme result = themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));
        return ThemeResponseDTO.from(result);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponseDTO> findAllThemes() {
        return themeRepository.findAll().stream().map(ThemeResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ThemeResponseDTO> getPopularThemes(Long weeks, Long limit) {
        LocalDate now = LocalDate.now(clock);
        return themeRepository.findPopularThemes(now.minusWeeks(weeks),
                now, limit).stream().map(ThemeResponseDTO::from).toList();
    }

    @Transactional
    public void deleteTheme(Long id) {
        validateThemeExists(id);
        validateRemovableTheme(id);
        themeRepository.delete(id);
    }

    private void validateThemeExists(Long id) {
        themeRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND)
        );
    }

    private void validateRemovableTheme(Long id) {
        if (reservationRepository.existByThemeId(id)) {
            throw new RoomEscapeException(ThemeErrorCode.RESERVATION_EXIST_ON_THEME);
        }
    }
}
