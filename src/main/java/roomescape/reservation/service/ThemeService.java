package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;
import roomescape.reservation.controller.dto.ThemeRequest;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Theme theme = new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        return ThemeResponse.from(themeRepository.save(theme));
    }

    public void delete(long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new BusinessException(ErrorType.RESERVATION_NOT_DELETED);
        }

        themeRepository.deleteById(themeId);
    }

    public List<ThemeResponse> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorType.INVALID_REQUEST_ERROR);
        }
        return themeRepository.findPopularThemes(startDate, endDate, limit).stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
