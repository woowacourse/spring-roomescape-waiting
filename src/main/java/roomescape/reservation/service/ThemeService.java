package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ThemeRequest;
import roomescape.reservation.dto.response.ThemeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int START_DATE_OFFSET = 8;
    private static final int END_DATE_OFFSET = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> getAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public ThemeResponse create(final ThemeRequest request) {
        Theme theme = themeRepository.save(request.toEntity());
        return ThemeResponse.from(theme);
    }

    @Transactional
    public void delete(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new AlreadyInUseException("Theme with id " + id + " is already in use");
        }
        if (!themeRepository.existsById(id)) {
            throw new EntityNotFoundException("존재하지 않는 테마입니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getPopularThemes() {
        LocalDate now = LocalDate.now();

        LocalDate startDate = now.minusDays(START_DATE_OFFSET);
        LocalDate endDate = now.minusDays(END_DATE_OFFSET);
        int popularThemeCount = 10;

        return themeRepository.findTopByDateAndCount(startDate, endDate, popularThemeCount)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
