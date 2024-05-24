package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.BadRequestException;
import roomescape.reservation.controller.dto.ThemeRequest;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationSlotRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationSlotRepository reservationSlotRepository) {
        this.themeRepository = themeRepository;
        this.reservationSlotRepository = reservationSlotRepository;
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
        if (reservationSlotRepository.existsByThemeId(themeId)) {
            throw new BadRequestException("예약이 존재하여 삭제할 수 없습니다.");
        }

        themeRepository.deleteById(themeId);
    }

    public List<ThemeResponse> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("올바르지 않는 데이터 요청입니다.");
        }
        return themeRepository.findPopularThemes(startDate, endDate, limit).stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
