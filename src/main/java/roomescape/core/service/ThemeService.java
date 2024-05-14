package roomescape.core.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Theme;
import roomescape.core.dto.theme.ThemeRequest;
import roomescape.core.dto.theme.ThemeResponse;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ThemeResponse create(final ThemeRequest request) {
        final Theme theme = new Theme(request.getName(), request.getDescription(), request.getThumbnail());
        validateDuplicatedName(theme);
        final Long id = themeRepository.save(theme);

        return new ThemeResponse(id, theme);
    }

    private void validateDuplicatedName(final Theme theme) {
        final Integer themeCount = themeRepository.countByName(theme.getName());
        if (themeCount > 0) {
            throw new IllegalArgumentException("해당 이름의 테마가 이미 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findPopularTheme() {
        final ZoneId kst = ZoneId.of("Asia/Seoul");
        final LocalDate today = LocalDate.now(kst);
        final LocalDate lastWeek = today.minusWeeks(1);
        return reservationRepository.findPopularTheme(today, lastWeek)
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional
    public void delete(final long id) {
        final int reservationCount = reservationRepository.countByThemeId(id);
        if (reservationCount > 0) {
            throw new IllegalArgumentException("예약 내역이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }
}
