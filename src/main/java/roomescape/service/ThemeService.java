package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.dto.business.ThemeCreationContent;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ThemeService(
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findTopThemes(LocalDate from, LocalDate to, int size) {
        List<Theme> themes = themeRepository.findThemesOrderByReservationCount(from, to, size);
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public ThemeResponse addTheme(ThemeCreationContent request) {
        Theme theme = Theme.createWithoutId(request.name(), request.description(), request.thumbnail());
        Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    public void deleteThemeById(Long id) {
        Theme theme = getThemeById(id);
        validateReservationInTheme(theme);
        validateMemberInTime(theme);
        themeRepository.deleteById(id);
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 회원은 존재하지 않습니다."));
    }

    private void validateReservationInTheme(Theme theme) {
        boolean isExistReservation = reservationRepository.existsByTheme(theme);
        if (isExistReservation) {
            throw new BadRequestException("이미 예약이 존재하는 테마입니다.");
        }
    }

    private void validateMemberInTime(Theme theme) {
        boolean isExistWaiting = waitingRepository.existsByTheme(theme);
        if (isExistWaiting) {
            throw new BadRequestException("이미 예약이 존재하는 예약시간입니다.");
        }
    }
}
