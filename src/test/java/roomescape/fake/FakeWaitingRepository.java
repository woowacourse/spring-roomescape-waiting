package roomescape.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> waitings = new LinkedHashMap<>();
    private Long idHolder = 1L;

    @Override
    public Boolean existsByDateAndThemeAndTime(LocalDate date, Long themeId, Long timeId) {
        return null;
    }

    @Override
    public Optional<WaitingDetail> findDetailById(Long id) {
        Waiting waiting = waitings.get(id);
        if (waiting == null) {
            return Optional.empty();
        }
        return Optional.of(new WaitingDetail(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getThemeId(),
                "dummy theme",
                "dummy description",
                "https://dummy.com/image.jpg",
                waiting.getTimeId(),
                LocalTime.of(9, 0)
        ));
    }

    @Override
    public Optional<Waiting> findOldestByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId) {
        return waitings.values().stream()
                .filter(w -> w.getDate().equals(date)
                        && w.getThemeId().equals(themeId)
                        && w.getTimeId().equals(timeId))
                .min(Comparator.comparing(Waiting::getId));
    }

    @Override
    public Waiting save(Waiting waiting) {
        Waiting saved = waiting.withId(idHolder++);
        waitings.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Integer delete(Long id) {
        return waitings.remove(id) != null ? 1 : 0;
    }

    @Override
    public List<WaitingOrderDetail> findByName(String name) {
        return waitings.values().stream()
                .filter(w -> w.getName().equals(name))
                .map(w -> new WaitingOrderDetail(
                        w.getId(),
                        w.getName(),
                        w.getDate(),
                        w.getThemeId(),
                        "dummy theme",
                        "dummy description",
                        "https://dummy.com/image.jpg",
                        w.getTimeId(),
                        LocalTime.of(9, 0),
                        1L
                ))
                .toList();
    }
}
