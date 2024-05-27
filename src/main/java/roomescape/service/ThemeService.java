package roomescape.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.domain.dto.ThemeRequest;
import roomescape.domain.dto.ThemeResponse;
import roomescape.exception.DeleteNotAllowException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ResponsesWrapper<ThemeResponse> findAll() {
        final List<ThemeResponse> themeResponses = themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
        return new ResponsesWrapper<>(themeResponses);
    }

    public ThemeResponse register(final ThemeRequest themeRequest) {
        final Theme theme = themeRepository.save(themeRequest.toEntity());
        return ThemeResponse.from(theme);
    }

    public void delete(final Long id) {
        validateExistReservation(id);
        themeRepository.deleteById(id);
    }

    public ResponsesWrapper<ThemeResponse> findPopularTheme(final LocalDate startDate, final LocalDate endDate, final Long count) {
        final List<Theme> themes = themeRepository.findPopularThemeByDate(startDate, endDate, PageRequest.of(0, count.intValue()));
        final List<ThemeResponse> themeResponses = themes.stream().map(ThemeResponse::from).toList();
        return new ResponsesWrapper<>(themeResponses);
    }

    private void validateExistReservation(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new DeleteNotAllowException("예약이 등록된 테마는 제거할 수 없습니다.");
        }
    }
}
