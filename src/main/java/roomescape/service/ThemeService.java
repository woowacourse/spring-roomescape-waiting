package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.PopularTheme;
import roomescape.domain.PopularThemeCondition;
import roomescape.domain.PopularThemePolicy;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final PopularThemePolicy popularThemePolicy;

    public ThemeService(
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository,
            PopularThemePolicy popularThemePolicy
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.popularThemePolicy = popularThemePolicy;
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        return themeRepository.insert(new Theme(null, name, description, thumbnail));
    }

    @Transactional
    public void delete(Long id) {
        validateDeletable(id);
        deleteTheme(id);
    }

    public List<PopularTheme> findWeeklyTopTen(LocalDate today) {
        PopularThemeCondition condition = popularThemePolicy.createCondition(today);
        return themeRepository.findPopular(condition);
    }

    private void deleteTheme(Long id) {
        try {
            themeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throwResourceInUse();
        }
    }

    private void validateDeletable(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throwResourceInUse();
        }
    }

    private void throwResourceInUse() {
        throw new RoomescapeException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 테마는 삭제할 수 없습니다.");
    }
}
