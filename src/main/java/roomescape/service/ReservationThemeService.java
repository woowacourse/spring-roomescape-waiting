package roomescape.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTheme;
import roomescape.dto.request.ReservationThemeRequest;
import roomescape.dto.response.ReservationThemeResponse;
import roomescape.domain.ReservationThemeRepository;

@Service
public class ReservationThemeService {

    public static final int DELETE_FAILED_COUNT = 0;
    private final ReservationThemeRepository reservationThemeRepository;

    public ReservationThemeService(
            final ReservationThemeRepository reservationThemeRepository) {
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
        final ReservationTheme reservationTheme = ReservationTheme.builder()
                .name(request.name())
                .description(request.description())
                .thumbnail(request.thumbnail())
                .build();
        validateUniqueThemes(reservationTheme);
        ReservationTheme saved = reservationThemeRepository.save(reservationTheme);
        return ReservationThemeResponse.from(saved);
    }

    public void removeReservationTheme(final long id) {
        reservationThemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
        try {
            reservationThemeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("예약이 존재해 테마를 삭제할 수 없습니다.");
        }
    }

    private void validateUniqueThemes(final ReservationTheme reservationTheme) {
        if (reservationThemeRepository.existsByName(reservationTheme.getName())) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 테마 입니다.");
        }
    }
}
