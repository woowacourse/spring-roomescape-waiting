package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.service.dto.ThemeCreate;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public ThemeResponse create(ThemeCreate themeCreate) {
        Theme theme = new Theme(themeCreate.name(), themeCreate.description(), themeCreate.thumbnail());
        return ThemeResponse.from(themeRepository.save(theme));
    }

    @Transactional
    public void delete(long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new BadRequestException(ErrorType.RESERVATION_NOT_DELETED);
        }

        themeRepository.deleteById(themeId);
    }

    public List<ThemeResponse> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(ErrorType.INVALID_REQUEST_ERROR);
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        return themeRepository.findTopThemesByReservations(startDate, endDate, pageRequest).stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
