package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.result.PopularThemeResult;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final JpaThemeRepository themeRepository;
    private final ThemeRepository themeQueryRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ThemeService(JpaThemeRepository themeRepository,
                        ThemeRepository themeQueryRepository,
                        ReservationRepository reservationRepository,
                        ReservationWaitingRepository reservationWaitingRepository) {
        this.themeRepository = themeRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        Theme theme = new Theme(null, name, description, thumbnail);
        return themeRepository.save(theme);
    }

    @Transactional
    public void delete(Long id) {
        validateDeletable(id);
        themeRepository.deleteById(id);
    }

    public List<PopularThemeResult> findWeeklyTopTen() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);
        //todo: 레거시 themeRepository 삭제 예정
        return themeQueryRepository.findPopular(startDate, endDate, 10);
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
