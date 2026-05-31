package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.result.PopularThemeResult;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ThemeService(ThemeRepository themeRepository,
                        ReservationRepository reservationRepository,
                        ReservationWaitingRepository reservationWaitingRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
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
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        if (reservationWaitingRepository.existsByThemeId(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약 대기가 존재하는 테마는 삭제할 수 없습니다.");
        }
    }
}
