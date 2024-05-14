package roomescape.service.theme;

import org.springframework.stereotype.Service;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeDeleteService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeDeleteService(ThemeRepository themeRepository,
                              ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void deleteTheme(long id) {
        themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마 아이디 입니다."));

        if (reservationRepository.existsByReservationThemeId(id)) {
            throw new IllegalArgumentException("이미 예약중인 테마는 삭제할 수 없습니다.");
        }

        themeRepository.deleteById(id);
    }
}
