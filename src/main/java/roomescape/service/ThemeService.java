package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.PopularThemeResult;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        Theme theme = new Theme(null, name, description, thumbnail);
        Long id = themeRepository.insert(theme);
        return themeRepository.findBy(id)
                .orElseThrow(() -> new IllegalArgumentException("생성된 테마를 찾을 수 없습니다."));
    }

    @Transactional
    public void delete(Long id) {
        validateDeletable(id);
        themeRepository.delete(id);
    }

    public List<PopularThemeResult> findWeeklyTopTen() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);
        return themeRepository.findPopular(startDate, endDate, 10);
    }

    private void validateDeletable(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new RoomescapeException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
    }
}
