package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.request.ThemeSaveRequest;
import roomescape.reservation.controller.dto.response.ThemeDeleteResponse;
import roomescape.reservation.controller.dto.response.ThemeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationService reservationService;

    public ThemeService(ThemeRepository themeRepository, ReservationService reservationService) {
        this.themeRepository = themeRepository;
        this.reservationService = reservationService;
    }

    public ThemeResponse save(final ThemeSaveRequest themeSaveRequest) {
        Theme theme = themeSaveRequest.toEntity();
        return ThemeResponse.from(themeRepository.save(theme));
    }

    public List<ThemeResponse> getAll() {
        return StreamSupport.stream(themeRepository.findAll().spliterator(), false)
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findThemeRanking() {
        Map<Long, List<Reservation>> collect1 = reservationService.some().stream()
                .filter(reservation -> reservation.getDate().isBefore(LocalDate.now()) && reservation.getDate()
                        .isAfter(LocalDate.now().minusWeeks(1)))
                .collect(Collectors.groupingBy(Reservation::getThemeId));

        List<Long> list = collect1.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Entry::getKey)
                .limit(10)
                .toList();

        return list.stream()
                .map(id -> ThemeResponse.from(themeRepository.findById(id).get()))
                .toList();
    }

    public ThemeDeleteResponse delete(final long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (id : " + id + ") 에 대한 테마가 존재하지 않습니다.");
        }
        reservationService.validateAlreadyHasReservationByThemeId(id);
        return new ThemeDeleteResponse(themeRepository.deleteById(id));
    }
}
