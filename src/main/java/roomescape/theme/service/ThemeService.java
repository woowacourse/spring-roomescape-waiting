package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.controller.dto.CreateThemeWebRequest;
import roomescape.theme.controller.dto.ThemeWebResponse;
import roomescape.theme.service.converter.ThemeConverter;
import roomescape.theme.service.usecase.ThemeCommandUseCase;
import roomescape.theme.service.usecase.ThemeQueryUseCase;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeQueryUseCase themeQueryUseCase;
    private final ThemeCommandUseCase themeCommandUseCase;

    public List<ThemeWebResponse> getAll() {
        return ThemeConverter.toDto(themeQueryUseCase.getAll());
    }

    public List<ThemeWebResponse> getRanking() {
        final int count = 10;
        final int dateRange = 7;
        final LocalDate endDate = LocalDate.now();
        final LocalDate startDate = endDate.minusDays(dateRange);

        return ThemeConverter.toDto(
                themeQueryUseCase.getRanking(
                        ReservationDate.from(startDate),
                        ReservationDate.from(endDate),
                        PageRequest.of(0, count)));
    }

    public ThemeWebResponse create(final CreateThemeWebRequest createThemeWebRequest) {
        return ThemeConverter.toDto(themeCommandUseCase.create(ThemeConverter.toServiceDto(createThemeWebRequest)));
    }

    public void delete(final Long id) {
        themeCommandUseCase.delete(id);
    }
}
