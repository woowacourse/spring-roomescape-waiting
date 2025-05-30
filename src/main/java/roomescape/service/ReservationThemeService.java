package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTheme;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.ReservationThemeRequest;
import roomescape.service.dto.ReservationThemeResponse;
import roomescape.repository.ReservationThemeRepository;

@Service
public class ReservationThemeService {

    private final ReservationRepository reservationRepository;
    private final ReservationThemeRepository reservationThemeRepository;

    public ReservationThemeService(
            final ReservationRepository reservationRepository, final ReservationThemeRepository reservationThemeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationThemeRepository = reservationThemeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationThemeResponse> findReservationThemes() {
        List<ReservationTheme> reservationThemes = reservationThemeRepository.findAll();
        return reservationThemes.stream().map(ReservationThemeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationThemeResponse> findPopularThemes() {
        List<ReservationTheme> popularReservationThemes = reservationThemeRepository.findWeeklyThemeOrderByCountDesc();
        return popularReservationThemes.stream().map(ReservationThemeResponse::from).toList();
    }

    @Transactional
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

    @Transactional
    public void removeReservationTheme(final long id) {
        validateExistTheme(id);
        validateExistReservation(id);
        reservationThemeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ReservationTheme getById(final long id) {
        return reservationThemeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("[ERROR] 테마를 찾을 수 없습니다."));
    }

    private void validateExistTheme(final long id) {
        if (!reservationThemeRepository.existsById(id)) {
            throw new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다.");
        }
    }

    private void validateExistReservation(final long id) {
        if (reservationRepository.existByThemeId(id)) {
            throw new IllegalArgumentException("[ERROR] 예약이 존재하는 테마이므로 삭제할 수 없습니다.");
        }
    }

    private void validateUniqueThemes(final ReservationTheme reservationTheme) {
        if (reservationThemeRepository.existsByName(reservationTheme.getName())) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 테마 입니다.");
        }
    }
}
