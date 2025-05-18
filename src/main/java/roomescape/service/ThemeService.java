package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.request.ThemeRegisterDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.model.Reservation;
import roomescape.model.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_DAY_RANGE = 7;
    private static final int POPULAR_THEME_SIZE = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponseDto> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponseDto::new)
                .collect(Collectors.toList());
    }

    public ThemeResponseDto saveTheme(ThemeRegisterDto themeRegisterDto) {
        validateTheme(themeRegisterDto);

        Theme theme = themeRegisterDto.convertToTheme();
        Theme savedTheme = themeRepository.save(theme);

        return new ThemeResponseDto(
                savedTheme.getId(),
                savedTheme.getName(),
                savedTheme.getDescription(),
                savedTheme.getThumbnail()
        );
    }

    private void validateTheme(ThemeRegisterDto themeRegisterDto) {
        boolean duplicatedNameExisted = themeRepository.existsByName(themeRegisterDto.name());
        if (duplicatedNameExisted) {
            throw new DuplicatedException("중복된 테마명은 등록할 수 없습니다.");
        }
    }

    public void deleteTheme(Long id) {
        clearThemeInReservations(id);
        themeRepository.deleteById(id);
    }

    public List<ThemeResponseDto> findPopularThemes(String date) {
        LocalDate parsedDate = LocalDate.parse(date);

        return themeRepository.findTopReservedThemesSince(
                        parsedDate,
                        parsedDate.minusDays(POPULAR_DAY_RANGE),
                        POPULAR_THEME_SIZE).stream()
                .map(theme -> new ThemeResponseDto(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnail()))
                .toList();
    }

    private void clearThemeInReservations(Long id) {
        List<Reservation> reservations = reservationRepository.findByThemeId(id);
        for (Reservation reservation : reservations) {
            reservation.setTheme(null);
        }
    }
}


