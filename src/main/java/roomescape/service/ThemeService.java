package roomescape.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ServiceThemeCreateRequest;
import roomescape.service.dto.response.ServiceThemeResponse;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    public static final int RANKING_LIMIT = 10;
    public static final int MAX_RANKING_PERIOD = 366;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ServiceThemeResponse save(ServiceThemeCreateRequest requestDto) {
        Theme theme = requestDto.toEntity();
        return ServiceThemeResponse.from(themeRepository.save(theme));
    }

    public List<ServiceThemeResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ServiceThemeResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        validateReferencedTheme(id);
        themeRepository.delete(id);
    }

    private void validateReferencedTheme(Long id) {
        if (reservationRepository.existByThemeId(id)) {
            throw new CustomInvalidRequestException(ErrorCode.REFERENCED_THEME);
        }
    }

    public List<ServiceThemeResponse> findRanking(LocalDate startDate, LocalDate endDate) {
        validateRankingPeriod(startDate, endDate);

        return themeRepository.findRanking(startDate, endDate, RANKING_LIMIT).stream()
                .map(ServiceThemeResponse::from)
                .toList();
    }

    private void validateRankingPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDate localDate = LocalDate.now();

        if (startDate.isAfter(localDate) || endDate.isAfter(localDate)) {
            throw new CustomInvalidRequestException(ErrorCode.FUTURE_RANKING_PERIOD);
        }
        if (startDate.isAfter(endDate)) {
            throw new CustomInvalidRequestException(ErrorCode.INVALID_RANKING_PERIOD);
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_RANKING_PERIOD) {
            throw new CustomInvalidRequestException(ErrorCode.LONG_RANKING_PERIOD);
        }
    }

    public Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_THEME));
    }
}
