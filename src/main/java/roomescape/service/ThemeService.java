package roomescape.service;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.service.dto.command.ThemeCommand;
import roomescape.service.dto.result.ReservationTimeDetailResult;
import roomescape.service.dto.result.ThemeResult;

@Service
public class ThemeService {
    private static final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/images/";

    private final ThemeDao themeDao;
    private final Clock clock;
    private final ReservationTimeDao reservationTimeDao;

    public ThemeService(ThemeDao themeDao, Clock clock, ReservationTimeDao reservationTimeDao) {
        this.themeDao = themeDao;
        this.clock = clock;
        this.reservationTimeDao = reservationTimeDao;
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
        MultipartFile file = command.file();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            file.transferTo(new File(filePath));

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장에 실패했습니다.", e);
        }

        String imageUrl = "/images/" + fileName;

        Theme theme = new Theme(
                null,
                ThemeName.parse(command.name()),
                Description.parse(command.description()),
                ThumbnailUrl.parse(imageUrl)
        );

        Theme saved = themeDao.save(theme);

        return ThemeResult.from(saved);
    }

    public void deleteTheme(Long id) {
        themeDao.delete(id);
    }

    public List<ReservationTimeDetailResult> findThemeSchedulesByDate(Long id, LocalDate date) {
        List<TimeQueryResult> availableTimes = reservationTimeDao.findStatuesByThemeIdAndDate(id, date);

        return availableTimes.stream()
                .map(ReservationTimeDetailResult::from)
                .toList();
    }
}
