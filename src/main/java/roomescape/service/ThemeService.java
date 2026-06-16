package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.ThemeRequest;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ScheduleDao;
import roomescape.repository.ThemeDao;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private static final int DEFAULT_POPULAR_PERIOD = 7;
    private static final int DEFAULT_POPULAR_LIMIT = 10;

    private final ThemeDao themeDao;
    private final ScheduleDao scheduleDao;

    public ThemeService(ThemeDao themeDao, ScheduleDao scheduleDao) {
        this.themeDao = themeDao;
        this.scheduleDao = scheduleDao;
    }

    @Transactional
    public Long saveTheme(ThemeRequest request) {
        validateDuplicateName(request.name());

        try {
            return themeDao.save(request.name(), request.description(), request.thumbnailUrl());
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_THEME_NAME, "존재하는 테마는 추가할 수 없습니다.");
        }
    }

    @Transactional
    public void deleteTheme(long id) {
        if (scheduleDao.existsByThemeId(id)) {
            throw new RoomescapeException(DomainErrorCode.REFERENTIAL_INTEGRITY,
                    "이 테마를 참조하는 예약이 있어 삭제할 수 없습니다. ID: " + id);
        }

        try {
            themeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomescapeException(DomainErrorCode.REFERENTIAL_INTEGRITY, "사용중인 테마는 삭제할 수 없습니다.");
        }
    }

    public List<Theme> findAll() {
        return themeDao.findAll();
    }

    public List<Theme> findPopularThemes() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(DEFAULT_POPULAR_PERIOD);
        return themeDao.findPopularThemes(
                startDate,
                endDate,
                ReservationStatus.RESERVED,
                DEFAULT_POPULAR_LIMIT
        );
    }

    private void validateDuplicateName(String name) {
        if (themeDao.existsByName(name)) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_THEME_NAME, "이미 존재하는 테마 이름입니다.");
        }
    }
}
