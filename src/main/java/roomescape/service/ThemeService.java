package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRegisterDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@RequiredArgsConstructor
@Service
public class ThemeService {

    private static final int POPULAR_DAY_RANGE = 7;
    private static final int POPULAR_THEME_SIZE = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<ThemeResponseDto> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ThemeResponseDto saveTheme(final ThemeRegisterDto themeRegisterDto) {
        validateTheme(themeRegisterDto);

        final Theme theme = themeRegisterDto.convertToTheme();
        final Theme savedTheme = themeRepository.save(theme);

        return new ThemeResponseDto(
                savedTheme.getId(),
                savedTheme.getName(),
                savedTheme.getDescription(),
                savedTheme.getThumbnail()
        );
    }

    private void validateTheme(final ThemeRegisterDto themeRegisterDto) {
        final boolean duplicatedNameExisted = themeRepository.existsByName(themeRegisterDto.name());
        if (duplicatedNameExisted) {
            throw new DuplicatedException("중복된 테마명은 등록할 수 없습니다.");
        }
    }

    @Transactional
    public void deleteTheme(final Long id) {
        validateAlreadyReservationForTheme(id);
        themeRepository.deleteById(id);
    }

    private void validateAlreadyReservationForTheme(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ResourceInUseException("삭제하고자 하는 테마에 예약된 정보가 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ThemeResponseDto> findPopularThemes(final String date) {
        final LocalDate startDate = LocalDate.parse(date);
        final LocalDate endDate = startDate.minusDays(POPULAR_DAY_RANGE);
        final PageRequest pageRequest = PageRequest.of(0, POPULAR_THEME_SIZE);

        return themeRepository.findTopReservedThemesSince(
                        startDate,
                        endDate,
                        pageRequest).stream()
                .map(theme -> new ThemeResponseDto(
                        theme.getId(),
                        theme.getName(),
                        theme.getDescription(),
                        theme.getThumbnail()))
                .toList();
    }
}


