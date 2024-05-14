package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.RoomTheme;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.RoomThemeResponse;

@Service
public class RoomThemeService {
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
        return roomThemeRepository.findAllRanking()
                .stream()
                .map(RoomThemeResponse::from)
                .toList();
    }

    public RoomThemeResponse save(RoomThemeCreateRequest roomThemeCreateRequest) {
        RoomTheme roomTheme = roomThemeCreateRequest.toRoomTheme();
        RoomTheme savedRoomTheme = roomThemeRepository.save(roomTheme);
        return RoomThemeResponse.from(savedRoomTheme);
    }

    public boolean deleteById(Long id) {
        return roomThemeRepository.deleteById(id);
    }
}
