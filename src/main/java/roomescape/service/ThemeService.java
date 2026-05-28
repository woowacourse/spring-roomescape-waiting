package roomescape.service;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    public ThemeService(ThemeDao themeDao, Clock clock) {
        this.themeDao = themeDao;
        this.clock = clock;
    }

    public List<ThemeResult> findAllThemes() {
        List<Theme> themes = themeDao.findAllThemes();
        return themes.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public List<ThemeResult> findTopTheme(Long count) {
        LocalDate today = LocalDate.now(clock);
        List<Theme> topTheme = themeDao.findTopThemes(count, today);
        return topTheme.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public ThemeResult create(ThemeCommand command) {
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

    public void delete(Long id) {
        themeDao.delete(id);
    }

    public List<ReservationTimeDetailResult> findThemeSchedule(Long id, LocalDate date) {
        List<TimeQueryResult> availableTimes = themeDao.findTimeStatusBy(id, date);

        return availableTimes.stream()
                .map(ReservationTimeDetailResult::from)
                .toList();
    }
}
