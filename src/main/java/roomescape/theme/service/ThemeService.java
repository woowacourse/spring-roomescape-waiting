package roomescape.theme.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.reservation.repository.MemberReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ThemeService(ThemeRepository themeRepository,
                        ReservationRepository reservationRepository,
                        MemberReservationRepository memberReservationRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public ThemeResponse createTheme(ThemeCreateRequest request) {
        Theme theme = request.toTheme();
        validateDuplicated(theme);
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    private void validateDuplicated(Theme theme) {
        themeRepository.findByName(theme.getName())
                .ifPresent(theme::validateDuplicatedName);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> readThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> readPopularThemes() {
        LocalDate end = LocalDate.now().minusDays(1L);
        LocalDate start = end.minusDays(Theme.POPULAR_THEME_PERIOD);

        Map<Long, Long> memberReservationCountByTheme = collectReservationByTheme(start, end);

        return memberReservationCountByTheme.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(Theme.POPULAR_THEME_COUNT)
                .map(e -> readTheme(e.getKey()))
                .toList();
    }

    private Map<Long, Long> collectReservationByTheme(LocalDate start, LocalDate end) {
        return memberReservationRepository.findByReservationDateBetween(start, end).stream()
                .collect(Collectors.groupingBy(
                        memberReservation -> memberReservation.getReservation().getTheme().getId(), Collectors.counting()
                ));
    }

    @Transactional(readOnly = true)
    public ThemeResponse readTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 테마입니다."));
        return ThemeResponse.from(theme);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTheme(Long id) {
        validateReservationExists(id);
        themeRepository.deleteById(id);
    }

    private void validateReservationExists(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new BadRequestException("해당 테마에 예약이 존재합니다.");
        }
    }
}
