package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.result.PopularThemeResult;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final JpaThemeRepository themeRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationWaitingRepository reservationWaitingRepository;

    public ThemeService(JpaThemeRepository themeRepository,
                        JpaReservationRepository reservationRepository,
                        JpaReservationWaitingRepository reservationWaitingRepository) {
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
        return themeRepository.findPopular(startDate, endDate, PageRequest.of(0, 10));
    }

    private void validateDeletable(Long id) {
        if (reservationRepository.existsByTheme_Id(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        if (reservationWaitingRepository.existsByTheme_Id(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약 대기가 존재하는 테마는 삭제할 수 없습니다.");
        }
    }
}
