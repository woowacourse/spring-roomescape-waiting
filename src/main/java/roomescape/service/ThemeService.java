package roomescape.service;

import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.DateRange;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationScheduleRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.request.CreateThemeRequest;
import roomescape.service.response.ThemeResponse;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;
    private final ReservationScheduleRepository reservationScheduleRepository;

    public ThemeService(
            final ThemeRepository themeRepository,
            final ReservationRepository reservationRepository,
            final Clock clock,
            final ReservationScheduleRepository reservationScheduleRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
        this.reservationScheduleRepository = reservationScheduleRepository;
    }

    @Transactional
    public void deleteThemeById(final Long id) {
        Optional<ReservationSchedule> schedule = reservationScheduleRepository.findByTheme_Id(id);
        if (schedule.isPresent() && reservationRepository.existsByScheduleId(schedule.get().getId())) {
            throw new IllegalStateException("이미 예약이 존재해서 테마를 삭제할 수 없습니다.");
        }
        Theme theme = getTheme(id);
        themeRepository.deleteById(theme.getId());
    }

    public ThemeResponse createTheme(final CreateThemeRequest request) {
        Theme theme = themeRepository.save(new Theme(
                null,
                new ThemeName(request.name()),
                new ThemeDescription(request.description()),
                new ThemeThumbnail(request.thumbnail())
        ));
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeResponse.from(themes);
    }

    public List<ThemeResponse> getWeeklyPopularThemes() {
        DateRange dateRange = DateRange.createLastWeekRange(clock);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Theme> themes = themeRepository.findPopularThemeDuringAWeek(
                dateRange.getStartDate(),
                dateRange.getEndDate(),
                pageRequest
        );
        return ThemeResponse.from(themes);
    }

    private Theme getTheme(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 테마가 존재하지 않습니다."));
    }
}
