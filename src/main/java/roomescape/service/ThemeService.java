package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.service.dto.command.ThemeCommand;
import roomescape.service.dto.result.ReservationTimeDetailResult;
import roomescape.service.dto.result.ThemeResult;

@Service
public class ThemeService {

    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final ReservationTimeDao reservationTimeDao;
    private final FileUploader fileUploader;
    private final Clock clock;

    public ThemeService(
            ReservationDao reservationDao,
            ThemeDao themeDao,
            ReservationTimeDao reservationTimeDao,
            FileUploader fileUploader,
            Clock clock
    ) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.reservationTimeDao = reservationTimeDao;
        this.fileUploader = fileUploader;
        this.clock = clock;
    }

    public List<ThemeResult> findThemes() {
        List<Theme> themes = themeDao.findAllThemes();
        return themes.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public List<ThemeResult> findPopularThemes(int limit) {
        LocalDate today = LocalDate.now(clock);
        List<Theme> popularThemes = themeDao.findPopularThemes(limit, today);
        return popularThemes.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public ThemeResult registerTheme(ThemeCommand command) {
        String imageUrl = fileUploader.upload(command.file());

        Theme theme = new Theme(
                ThemeName.parse(command.name()),
                Description.parse(command.description()),
                ThumbnailUrl.parse(imageUrl)
        );

        Theme saved = themeDao.save(theme);

        return ThemeResult.from(saved);
    }

    public void deleteTheme(Long id) {
        if (reservationDao.existsByThemeId(id)) {
            throw new ConflictException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }

        themeDao.delete(id);
    }

    public List<ReservationTimeDetailResult> findThemeSchedulesByDate(Long id, LocalDate date) {
        List<TimeQueryResult> availableTimes = reservationTimeDao.findStatuesByThemeIdAndDate(id, date);

        return availableTimes.stream()
                .map(ReservationTimeDetailResult::from)
                .toList();
    }
}
