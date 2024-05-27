package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.reservation.dto.PopularThemeResponse;
import roomescape.controller.theme.dto.CreateThemeRequest;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.ThemeUsedException;

@Service
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(final ReservationRepository reservationRepository,
                        final ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse addTheme(final CreateThemeRequest createThemeRequest) {
        final Theme theme = createThemeRequest.toDomain();
        final Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    public void deleteTheme(final long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ThemeUsedException("예약된 테마는 삭제할 수 없습니다.");
        }
        final Theme findTheme = themeRepository.findByIdOrThrow(id);
        themeRepository.deleteById(findTheme.getId());
    }

    public List<PopularThemeResponse> findMostBookedThemes(final LocalDate from,
                                                           final LocalDate until,
                                                           final int limit) {
        if (from.isAfter(until)) {
            throw new InvalidRequestException("유효하지 않은 날짜 범위입니다.");
        }
        final Reservations reservations = new Reservations(
                reservationRepository.findAllJoinThemeByDateBetween(from, until));
        return reservations.findMostBookedThemes(limit)
                .stream()
                .map(PopularThemeResponse::from)
                .toList();
    }
}
