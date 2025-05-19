package roomescape.business.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;
import roomescape.util.CurrentUtil;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final CurrentUtil currentUtil;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository, final CurrentUtil currentUtil) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.currentUtil = currentUtil;
    }

    @Transactional
    public ThemeResponse insert(final ThemeRequest themeRequest) {
        validateNameIsNotDuplicate(themeRequest.name());
        final Theme theme = themeRequest.toDomain();
        final Long id = themeRepository.save(theme)
                .getId();
        return new ThemeResponse(id, theme.getName(), theme.getDescription(), theme.getThumbnail());
    }

    private void validateNameIsNotDuplicate(final String name) {
        if (themeRepository.existsByName(name)) {
            throw new DuplicateException("추가 하려는 테마 이름이 이미 존재합니다.");
        }
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse findById(final Long id) {
        final Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(id)));
        return ThemeResponse.from(theme);
    }

    @Transactional
    public void deleteById(final Long id) {
        if (!themeRepository.existsById(id)) {
            throw new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(id));
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findPopularThemes() {
        final LocalDate now = currentUtil.getCurrentDate();
        final LocalDate endDate = now;
        final LocalDate startDate = now.minusDays(7);
        return findPopularThemesBetween(startDate, endDate);
    }

    private List<ThemeResponse> findPopularThemesBetween(final LocalDate startDate, final LocalDate endDate) {
        final List<Theme> themes = themeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findByDateBetween(startDate, endDate);
        final Map<Long, Long> themeReservationCount = calculateThemeReservationCount(reservations);
        final List<Theme> sortedThemes = sortedThemesByReservationCount(themes, themeReservationCount);
        return sortedThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    private Map<Long, Long> calculateThemeReservationCount(final List<Reservation> reservations) {
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getTheme()
                                .getId(),
                        Collectors.counting()
                ));
    }

    private List<Theme> sortedThemesByReservationCount(final List<Theme> themes,
                                                       final Map<Long, Long> themeReservationCount) {
        return themes.stream()
                .sorted((theme1, theme2) -> {
                    final Long theme1ReservationCount = themeReservationCount.getOrDefault(theme1.getId(), 0L);
                    final Long theme2ReservationCount = themeReservationCount.getOrDefault(theme2.getId(), 0L);
                    return theme2ReservationCount.compareTo(theme1ReservationCount);
                })
                .toList();
    }
}
