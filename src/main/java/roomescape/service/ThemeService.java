package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.PopularTheme;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.theme.command.CreateThemeCommand;
import roomescape.dto.theme.response.ThemeReservationTimeResponse;
import roomescape.dto.theme.response.ThemeResponses;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final Integer POPULAR_THEME_PERIOD_DAYS = 7;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository,
                        ReservationTimeRepository reservationTimeRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ThemeResponses getThemes(int page, int size) {
        List<Theme> themes = themeRepository.findAll(size + 1, page * size);
        boolean hasNext = themes.size() > size;
        if (hasNext) {
            themes = themes.subList(0, size);
        }
        return ThemeResponses.of(themes, hasNext);
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마을(를) 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public Theme createTheme(CreateThemeCommand command) {
        Theme theme = new Theme(null, command.name(), command.description(), command.thumbnailImageUrl());
        Long newThemeId = themeRepository.save(theme);
        return theme.withId(newThemeId);
    }

    public void deleteTheme(Long id) {
        int affected = themeRepository.deleteById(id);
        if (affected == 0) {
            throw new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마을(를) 찾을 수 없습니다. id=" + id);
        }
    }

    @Transactional(readOnly = true)
    public List<ThemeReservationTimeResponse> getThemeTimes(Long themeId, LocalDate date) {
        List<Long> reservedTimeIds = reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(time -> new ThemeReservationTimeResponse(
                        time.getId(),
                        time.getStartAt().toString(),
                        reservedTimeIds.contains(time.getId())
                ))
                .toList();
    }

    public List<PopularTheme> getPopularThemes(Integer limit) {
        LocalDate today = LocalDate.now();
        LocalDate to = today.minusDays(1);
        LocalDate from = today.minusDays(POPULAR_THEME_PERIOD_DAYS);

        return themeRepository.findPopularThemes(from, to, limit);
    }
}
