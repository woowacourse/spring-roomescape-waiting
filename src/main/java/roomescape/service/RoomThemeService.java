package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.RoomTheme;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.RoomThemeResponse;

@Service
public class RoomThemeService {

    private static final int DEFAULT_BEST_THEME_COUNT = 10;

    private final RoomThemeRepository roomThemeRepository;

    public RoomThemeService(RoomThemeRepository roomThemeRepository) {
        this.roomThemeRepository = roomThemeRepository;
    }

    public List<RoomThemeResponse> findAll() {
        return roomThemeRepository.findAll()
                .stream()
                .map(RoomThemeResponse::from)
                .toList();
    }

    public List<RoomThemeResponse> findByRanking() {
        LocalDate dateTo = LocalDate.now().minusDays(1);
        LocalDate dateFrom = dateTo.minusDays(7);

        return roomThemeRepository.findMostReservedThemeInPeriodByCount(dateFrom, dateTo, DEFAULT_BEST_THEME_COUNT)
                .stream()
                .map(RoomThemeResponse::from)
                .toList();
    }

    public RoomThemeResponse save(RoomThemeCreateRequest roomThemeCreateRequest) {
        RoomTheme roomTheme = roomThemeCreateRequest.toRoomTheme();
        RoomTheme savedRoomTheme = roomThemeRepository.save(roomTheme);
        return RoomThemeResponse.from(savedRoomTheme);
    }

    public void deleteById(Long id) {
        roomThemeRepository.deleteById(id);
    }
}
