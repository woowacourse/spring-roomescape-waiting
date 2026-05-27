package roomescape.fake;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;

public class FakeWaitingRepository implements WaitingRepository {
    @Override
    public Optional<WaitingDetail> findDetailById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Waiting> findOldestByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId) {
        return Optional.empty();
    }

    @Override
    public Waiting save(Waiting waiting) {
        return null;
    }

    @Override
    public Integer delete(Long id) {
        return 0;
    }

    @Override
    public List<WaitingOrderDetail> findByName(String name) {
        return List.of();
    }
}
