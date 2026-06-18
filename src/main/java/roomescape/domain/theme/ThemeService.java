package roomescape.domain.theme;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.JpaReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.domain.theme.admin.dto.AdminThemeResponse;
import roomescape.domain.theme.admin.dto.CreateThemeRequest;
import roomescape.domain.theme.admin.dto.CreateThemeResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeRankResult;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_LIMIT = 7;

    private final JpaThemeRepository themeRepository;
    private final JpaReservationSlotRepository reservationSlotRepository;
    private final JpaReservationRepository reservationRepository;

    private final Clock clock;

    public List<AdminThemeResponse> getAllThemeForAdmin() {
        return themeRepository.findAll().stream()
            .map(AdminThemeResponse::from)
            .toList();
    }

    public CreateThemeResponse createTheme(CreateThemeRequest request) {
        Theme theme = themeRepository.save(request.toEntity());
        return CreateThemeResponse.from(theme);
    }

    public void deleteTheme(Long id) {
        if (reservationSlotRepository.countByThemeId(id) > 0) {
            throw new ConflictException(ThemeErrors.THEME_IN_USE);
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getAllTheme() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }

    public List<ThemeRankResponse> getThemeRank() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDay = today.minusDays(RANK_DAYS_LIMIT);
        List<Theme> reservedThemes = reservationRepository.findThemesForRanking(
            startDay,
            today,
            ReservationStatus.CANCELED
        );
        return calculateThemeRanks(reservedThemes).stream()
            .limit(RANK_LIMIT)
            .map(ThemeRankResponse::from)
            .toList();
    }

    private List<ThemeRankResult> calculateThemeRanks(List<Theme> reservedThemes) {
        Map<Long, Long> reservationCountByThemeId = reservedThemes.stream()
            .collect(Collectors.groupingBy(
                Theme::getId,
                Collectors.counting()
            ));
        Map<Long, Theme> themeById = reservedThemes.stream()
            .collect(Collectors.toMap(
                Theme::getId,
                Function.identity(),
                (firstTheme, ignored) -> firstTheme,
                LinkedHashMap::new
            ));

        List<Map.Entry<Long, Long>> sortedEntries = reservationCountByThemeId.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()))
            .toList();

        List<ThemeRankResult> results = new ArrayList<>();
        Long previousCount = null;
        int rank = 0;

        for (int index = 0; index < sortedEntries.size(); index++) {
            Map.Entry<Long, Long> entry = sortedEntries.get(index);
            Long reservationCount = entry.getValue();
            if (!reservationCount.equals(previousCount)) {
                rank = index + 1;
                previousCount = reservationCount;
            }

            Theme theme = themeById.get(entry.getKey());
            results.add(ThemeRankResult.of(
                theme.getId(),
                theme.getName(),
                theme.getUrl(),
                rank
            ));
        }

        return results;
    }

    public Theme findThemeByIdOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
    }
}
