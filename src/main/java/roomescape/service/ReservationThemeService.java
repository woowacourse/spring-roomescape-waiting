package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTheme;
import roomescape.dto.ReservationThemeRequest;
import roomescape.dto.ReservationThemeResponse;
import roomescape.repository.ReservationThemeRepository;

@Service
public class ReservationThemeService {

    public static final int DELETE_FAILED_COUNT = 0;
    private final ReservationThemeRepository reservationThemeRepository;

    public ReservationThemeService(final ReservationThemeRepository reservationThemeRepository) {
        this.reservationThemeRepository = reservationThemeRepository;
    }

    public List<ReservationThemeResponse> findReservationThemes() {
        List<ReservationTheme> reservationThemes = reservationThemeRepository.findAll();
        return reservationThemes.stream().map(ReservationThemeResponse::from).toList();
    }

    public List<ReservationThemeResponse> findPopularThemes() {
        List<ReservationTheme> popularReservationThemes = reservationThemeRepository.findWeeklyThemeOrderByCountDesc();
        return popularReservationThemes.stream().map(ReservationThemeResponse::from).toList();
    }

    public ReservationThemeResponse addReservationTheme(final ReservationThemeRequest request) {
        ReservationTheme reservationTheme = new ReservationTheme(request.name(), request.description(),
                request.thumbnail());
        validateUniqueThemes(reservationTheme);
        ReservationTheme saved = reservationThemeRepository.save(reservationTheme);
        return ReservationThemeResponse.from(saved);
    }

    public void removeReservationTheme(final long id) {
        int deleteCounts = reservationThemeRepository.deleteById(id);
        if (deleteCounts == DELETE_FAILED_COUNT) {
            throw new IllegalArgumentException(String.format("[ERROR] 예약테마 %d번은 존재하지 않습니다.", id));
        }
    }

    private void validateUniqueThemes(final ReservationTheme reservationTheme) {
        if (reservationThemeRepository.existsByName(reservationTheme.getName())) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 테마 입니다.");
        }
    }
}
