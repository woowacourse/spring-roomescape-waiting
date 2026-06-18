package roomescape.waiting.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.waiting.application.port.out.WaitingRepository;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;

@Repository
@RequiredArgsConstructor
public class JpaWaitingRepository implements WaitingRepository {
    private final SpringDataWaitingRepository repository;

    @Override
    public Waiting save(Waiting waiting) {
        return repository.save(waiting);
    }

    @Override
    public Optional<Waiting> findById(long waitingId) {
        return repository.findById(waitingId);
    }

    @Override
    public Optional<Waiting> findByIdForUpdate(long waitingId) {
        return repository.findByIdForUpdate(waitingId);
    }

    @Override
    public Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        return Set.copyOf(repository.findTimeIdsByDateAndThemeId(date, themeId));
    }

    @Override
    public boolean existsBySlotIdAndMemberId(long memberId, long slotId) {
        return repository.existsBySlot_IdAndMember_Id(slotId, memberId);
    }

    @Override
    public boolean existsBySlotId(long slotId) {
        return repository.existsBySlot_Id(slotId);
    }

    @Override
    public List<Waiting> findAllBySlotIdOrderById(long slotId) {
        return repository.findAllBySlot_IdOrderById(slotId);
    }

    @Override
    public List<Waiting> findAllBySlotIdOrderByIdForUpdate(long slotId) {
        return repository.findAllBySlotIdOrderByIdForUpdate(slotId);
    }

    @Override
    public List<Waiting> findAllBySlotIds(List<Long> slotIds) {
        if (slotIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllBySlot_IdIn(slotIds);
    }

    @Override
    public List<WaitingDetailProjection> findAllWaitingDetails() {
        return repository.findAllWaitingDetails();
    }

    @Override
    public List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId) {
        return repository.findAllWaitingDetailsByMemberId(memberId);
    }

    @Override
    public void deleteById(long waitingId) {
        repository.deleteById(waitingId);
    }
}
