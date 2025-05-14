package roomescape.service;

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
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_DAY_RANGE = 7;
    private static final int POPULAR_THEME_SIZE = 10;

    private final ThemeRepository themeRepository;

    public ThemeService(final ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponseDto> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponseDto::from)
                .collect(Collectors.toList());
    }

    public ThemeResponseDto saveTheme(final ThemeRegisterDto themeRegisterDto) {
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

    private void validateTheme(final ThemeRegisterDto themeRegisterDto) {
        boolean duplicatedNameExisted = themeRepository.existsByName(themeRegisterDto.name());
        if (duplicatedNameExisted) {
            throw new DuplicatedException("중복된 테마명은 등록할 수 없습니다.");
        }
    }

    public void deleteTheme(final Long id) {
        try {
            themeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 테마에 예약된 정보가 있습니다.");
        }
    }

    public List<ThemeResponseDto> findPopularThemes(final String date) {
        LocalDate parsedDate = LocalDate.parse(date);

        return themeRepository.findTopReservedThemesSince(
                        parsedDate,
                        parsedDate.plusDays(POPULAR_DAY_RANGE),
                        POPULAR_THEME_SIZE).stream()
                .map(theme -> new ThemeResponseDto(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnail()))
                .toList();
    }
}


