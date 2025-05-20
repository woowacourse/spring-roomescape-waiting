package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NoElementsException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void deleteById(Long id) {
        if (reservationRepository.hasReservationWithTheme(id)) {
            throw new InvalidArgumentException("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
        Theme theme = getTheme(id);
        themeRepository.deleteById(theme.getId());
    }

    @Transactional
    public ThemeResponse create(ThemeCreateRequest request) {
        Theme created = Theme.create(request.name(), request.description(), request.thumbnail());
        Theme saved = themeRepository.save(created);
        return ThemeResponse.from(saved);
    }

    public List<ThemeResponse> getAll() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoElementsException("해당 테마가 존재하지 않습니다."));
    }

    public List<ThemeResponse> getPopularThemes() {
        int limit = 10;
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(7);

        return themeRepository.findAll().stream()
                .sorted((t1, t2) -> Long.compare(
                        reservationRepository.countReservationByThemeIdAndDuration(from, to, t1.getId()),
                        reservationRepository.countReservationByThemeIdAndDuration(from, to, t2.getId())
                ))
                .limit(limit)
                .map(ThemeResponse::from)
                .toList();
    }
}
