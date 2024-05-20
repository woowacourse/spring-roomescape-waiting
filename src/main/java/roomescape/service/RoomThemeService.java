package roomescape.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.RoomTheme;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.RoomThemeResponse;

@Service
@Transactional
public class RoomThemeService {

    private static final int DEFAULT_BEST_THEME_COUNT = 10;

    private final RoomThemeRepository roomThemeRepository;

    public RoomThemeService(RoomThemeRepository roomThemeRepository) {
        this.roomThemeRepository = roomThemeRepository;
    }

    public ListResponse<RoomThemeResponse> findAll() {
        List<RoomThemeResponse> responses = roomThemeRepository.findAll()
                .stream()
                .map(RoomThemeResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ListResponse<RoomThemeResponse> findByRanking() {
        LocalDate dateTo = LocalDate.now().minusDays(1);
        LocalDate dateFrom = dateTo.minusDays(7);

        List<RoomThemeResponse> responses = roomThemeRepository.findMostReservedThemeInPeriodByCount(dateFrom, dateTo,
                        DEFAULT_BEST_THEME_COUNT)
                .stream()
                .map(RoomThemeResponse::from)
                .toList();

        return new ListResponse<>(responses);
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
