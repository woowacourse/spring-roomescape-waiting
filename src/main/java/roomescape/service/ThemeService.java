package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.REFERENTIAL_INTEGRITY;
import static roomescape.domain.exception.DomainErrorCode.THEME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ThemeRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> getThemes() {
        return themeRepository.findAll();
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
            .orElseThrow(() -> new RoomEscapeException(THEME_NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    public List<Theme> getPopularTop10Themes(LocalDate now, Integer days) {
        LocalDate start = now.minusDays(days);
        LocalDate end = now.minusDays(1);
        return themeRepository.getPopularTop10Themes(start, end);
    }

    @Transactional
    public Theme addTheme(ThemeRequest request) {
        return themeRepository.save(
            new Theme(
                request.name(),
                request.description(),
                request.thumbnailImageUrl()
            )
        );
    }

    @Transactional
    public void deleteTheme(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new RoomEscapeException(REFERENTIAL_INTEGRITY, "해당 테마를 사용 중인 예약이 존재하여 삭제할 수 없습니다.");
        }

        getTheme(id);
        themeRepository.deleteById(id);
    }
}
