package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.dto.ThemeRequest;
import roomescape.domain.dto.ThemeResponse;
import roomescape.domain.dto.ThemeResponses;
import roomescape.exception.DeleteNotAllowException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponses findAll() {
        final List<ThemeResponse> themeResponses = themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return new ThemeResponses(themeResponses);
    }

    @Transactional
    public ThemeResponse create(final ThemeRequest themeRequest) {
        final Theme theme = themeRepository.save(themeRequest.toEntity());
        return ThemeResponse.from(theme);
    }

    @Transactional
    public void delete(final Long id) {
        validateExistReservation(id);
        themeRepository.deleteById(id);
    }

    public ThemeResponses getPopularThemeList(final LocalDate startDate, final LocalDate endDate, final Long count) {
        final List<Theme> themes = themeRepository.findPopularThemeByDate(startDate, endDate, count);
        final List<ThemeResponse> themeResponses = themes.stream().map(ThemeResponse::from).toList();
        return new ThemeResponses(themeResponses);
    }

    private void validateExistReservation(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new DeleteNotAllowException("예약이 등록된 테마는 제거할 수 없습니다.");
        }
    }
}
