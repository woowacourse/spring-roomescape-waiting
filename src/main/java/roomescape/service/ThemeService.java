package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 테마 입니다"));
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toEntity();
        Theme createdTheme = themeRepository.save(theme);
        return ThemeResponse.from(createdTheme);
    }

    public void delete(Long id) {
        validateExistReservation(id);
        themeRepository.deleteById(id);
    }

    private void validateExistReservation(Long id) {
        Theme theme = findThemeById(id);
        if (reservationRepository.existsByTheme(theme)) {
            throw new IllegalArgumentException("[ERROR] 예약이 등록된 테마는 제거할 수 없습니다");
        }
    }
}
