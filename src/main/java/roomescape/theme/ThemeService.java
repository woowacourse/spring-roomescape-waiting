package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.ReservationDao;
import roomescape.theme.ThemeDao;
import roomescape.common.vo.Name;
import roomescape.theme.web.PopularThemeRequestDto;
import roomescape.theme.web.ThemeRequestDto;
import roomescape.theme.web.AvailableTimeResponseDto;

@Service
@Transactional
public class ThemeService {
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ThemeService(ThemeDao themeDao, ReservationDao reservationDao) {
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional(readOnly = true)
    public List<Theme> findAll() {
        return themeDao.findAll();
    }


    @Transactional(readOnly = true)
    public Theme findById(Long id) {
        return themeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
    }

    public Theme create(ThemeRequestDto themeRequest) {
        Name name = new Name(themeRequest.name());
        if (themeDao.existsByName(name)) {
            throw new DuplicateEntityException("이미 존재하는 테마 이름입니다.");
        }

        Theme theme = new Theme(name, themeRequest.thumbnailUrl(), themeRequest.description(), themeRequest.price());
        try {
            return themeDao.insert(theme);
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 존재하는 테마 이름입니다.");
        }
    }

    public void delete(Long id) {
        if (reservationDao.existsByThemeId(id)) {
            throw new BusinessRuleViolationException("예약이 존재하여 테마를 삭제할 수 없습니다.");
        }

        try {
            if (!themeDao.delete(id)) {
                throw new EntityNotFoundException("존재하지 않는 테마입니다.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new BusinessRuleViolationException("예약이 존재하여 테마를 삭제할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<AvailableTimeResponseDto> findAvailableTimesById(Long themeId, LocalDate localDate) {
        return themeDao.findAvailableTimesById(themeId, localDate);
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopulars(PopularThemeRequestDto popularThemeRequestDto) {
        LocalDate to = LocalDate.now();
        int minusDays = popularThemeRequestDto.days() - 1;
        LocalDate from = to.minusDays(minusDays);

        PopularThemes populars = new PopularThemes(themeDao.findReservationCounts(from, to));
        return populars.topN(popularThemeRequestDto.limit());
    }
}
