package roomescape.theme.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.reservation.repository.ReservationJpaRepository;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeJpaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThemeService {

    private final ThemeJpaRepository themeJpaRepository;
    private final ReservationJpaRepository reservationJpaRepository;

    public ThemeService(ThemeJpaRepository themeJpaRepository, ReservationJpaRepository reservationJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
        this.reservationJpaRepository = reservationJpaRepository;
    }

    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        validateDuplicatedName(themeRequest);
        try {
            Theme theme = themeJpaRepository.save(themeRequest.toTheme());
            return new ThemeResponse(theme);
        } catch (DuplicateKeyException e) {
            throw new DuplicationException("이미 존재하는 테마 이름입니다.");
        }
    }

    private void validateDuplicatedName(ThemeRequest themeRequest) {
        if (themeJpaRepository.existsByName(new Name(themeRequest.name()))) {
            throw new DuplicationException("이미 존재하는 테마 이름입니다.");
        }
    }

    public ThemeResponse getTheme(Long id) {
        Theme theme = themeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 id입니다. theme_id = " + id));

        return new ThemeResponse(theme);
    }

    public List<ThemeResponse> findTrendingThemes(Long limit) {
        LocalDate now = LocalDate.now();
        LocalDate trendingStatsStart = now.minusDays(7);
        LocalDate trendingStatsEnd = now.minusDays(1);

        List<Theme> mostReservedThemesBetweenDates = reservationJpaRepository.findTrendingThemesBetweenDates(trendingStatsStart, trendingStatsEnd, PageRequest.of(0, Math.toIntExact(limit)));
        return mostReservedThemesBetweenDates
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findThemes() {
        List<ThemeResponse> themeResponses = new ArrayList<>();
        for (Theme theme : themeJpaRepository.findAll()) {
            themeResponses.add(new ThemeResponse(theme));
        }
        return themeResponses;
    }

    public void deleteTheme(Long id) {
        themeJpaRepository.deleteById(id);
    }

    public Theme getById(Long themeId) {
        return themeJpaRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("id에 맞는 테마가 없습니다. themeId = " + themeId));
    }
}
