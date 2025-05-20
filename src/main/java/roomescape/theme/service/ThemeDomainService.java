package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeDomainService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeDomainService(final ThemeRepository themeRepository,
                              final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> getThemes() {
        return themeRepository.findAll();
    }

    public void delete(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ReservationTimeInUseException("해당 테마에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public Theme create(final ThemeCreateRequest request) {
        return themeRepository.save(request.toTheme());
    }

    public Page<Theme> getPopularThemes(int days, int limit) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        return themeRepository.findPopularThemes(
                startDate,
                endDate,
                PageRequest.of(0, limit)
        );
    }

    public Theme findTheme(final Long request) {
        return themeRepository.findById(request)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 테마 정보가 없습니다."));
    }

    public void deleteById(final Long id) {
        themeRepository.deleteById(id);
    }

    public Theme save(final Theme theme) {
        return themeRepository.save(theme);
    }

    public Page<Theme> findPopularThemes(final int days, final int limit) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        return themeRepository.findPopularThemes(startDate, endDate, PageRequest.of(0, limit));
    }

    public Theme findById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 테마 정보가 없습니다."));
    }
}
