package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private static final int NUMBER_OF_POPULAR = 10;
    private static final int DAYS_TO_SUBTRACT_AT_START_POPULAR = 7;
    private static final int DAYS_TO_SUBTRACT_AT_END_POPULAR = 1;

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Theme create(Theme theme) {
        return themeRepository.save(theme);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public Theme findById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 테마가 없습니다."));
    }

    @Transactional
    public void deleteById(Long id) {
        themeRepository.deleteById(id);
    }

    public List<Theme> findAllPopular() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS_TO_SUBTRACT_AT_START_POPULAR);
        LocalDate endDate = today.minusDays(DAYS_TO_SUBTRACT_AT_END_POPULAR);
        return themeRepository.findAllByDateBetweenAndOrderByReservationCount(startDate, endDate, NUMBER_OF_POPULAR);
    }
}
