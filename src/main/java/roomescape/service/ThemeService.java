package roomescape.service;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
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
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final Clock clock;

    public ThemeService(ThemeDao themeDao, WaitingDao waitingDao, ReservationDao reservationDao, Clock clock) {
        this.themeDao = themeDao;
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
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

    public ThemeResult createTheme(ThemeCommand command) {
        if (themeDao.existsByName(command.name())) {
            throw new ConflictException("이미 존재하는 테마입니다.");
        }

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
        themeDao.findThemeById(id).orElseThrow(
                () -> new NotFoundException("존재하지 않는 테마입니다."));

        if (reservationDao.existsByThemeId(id)) {
            throw new ConflictException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }

        if (waitingDao.existsByThemeId(id)) {
            throw new ConflictException("예약 대기가 존재하는 테마는 삭제할 수 없습니다.");
        }

        themeDao.delete(id);
    }

    public List<ReservationTimeDetailResult> findThemeSchedule(Long id, LocalDate date) {
        List<TimeQueryResult> availableTimes = themeDao.findTimeStatusBy(id, date);

        return availableTimes.stream()
                .map(ReservationTimeDetailResult::from)
                .toList();
    }
}