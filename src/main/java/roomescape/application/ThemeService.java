package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ThemeRequest;
import roomescape.application.dto.ThemeResponse;
import roomescape.domain.PopularThemeFinder;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ThemeCommandRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ThemeService {

    private final ThemeCommandRepository themeCommandRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final PopularThemeFinder popularThemeFinder;

    public ThemeService(ThemeCommandRepository themeCommandRepository, ThemeQueryRepository themeQueryRepository,
                        ReservationQueryRepository reservationQueryRepository, PopularThemeFinder popularThemeFinder) {
        this.themeCommandRepository = themeCommandRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.popularThemeFinder = popularThemeFinder;
    }

    public ThemeResponse create(ThemeRequest request) {
        Theme savedTheme = themeCommandRepository.save(request.toTheme());
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAll() {
        return themeQueryRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        Theme theme = themeQueryRepository.getById(id);
        if (reservationQueryRepository.existsByTheme(theme)) {
            throw new RoomescapeException(RoomescapeErrorCode.ALREADY_RESERVED,
                    String.format("해당 테마에 연관된 예약이 존재하여 삭제할 수 없습니다. 삭제 요청한 테마:%s", theme.getName()));
        }
        themeCommandRepository.delete(theme);
    }


    public List<ThemeResponse> findPopularThemes() {
        return popularThemeFinder.findThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
