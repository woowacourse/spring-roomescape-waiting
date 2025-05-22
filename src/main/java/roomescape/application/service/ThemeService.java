package roomescape.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.dto.request.ThemeRegisterDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.model.Theme;
import roomescape.infrastructure.db.ReservationJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;

@Service
public class ThemeService {

    private static final int POPULAR_DAY_RANGE = 7;
    private static final int POPULAR_THEME_SIZE = 10;

    private final ThemeJpaRepository themeJpaRepository;
    private final ReservationJpaRepository reservationJpaRepository;

    public ThemeService(ThemeJpaRepository themeJpaRepository, ReservationJpaRepository reservationJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
        this.reservationJpaRepository = reservationJpaRepository;
    }

    public List<ThemeResponseDto> getAllThemes() {
        return themeJpaRepository.findAll().stream()
                .map(ThemeResponseDto::new)
                .collect(Collectors.toList());
    }

    public ThemeResponseDto saveTheme(ThemeRegisterDto themeRegisterDto) {
        validateTheme(themeRegisterDto);

        Theme theme = themeRegisterDto.convertToTheme();
        Theme savedTheme = themeJpaRepository.save(theme);

        return new ThemeResponseDto(
                savedTheme.getId(),
                savedTheme.getName(),
                savedTheme.getDescription(),
                savedTheme.getThumbnail()
        );
    }

    private void validateTheme(ThemeRegisterDto themeRegisterDto) {
        boolean duplicatedNameExisted = themeJpaRepository.existsByName(themeRegisterDto.name());
        if (duplicatedNameExisted) {
            throw new DuplicatedException("중복된 테마명은 등록할 수 없습니다.");
        }
    }

    public void deleteTheme(Long id) {
        try {
            themeJpaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 테마에 예약된 정보가 있습니다.");
        }
    }

    public List<ThemeResponseDto> findPopularThemes(String date) {
        LocalDate parsedDate = LocalDate.parse(date);

        return themeJpaRepository.findTopReservedThemesSince(
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
}


