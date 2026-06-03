package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import roomescape.domain.Reservation;
import roomescape.domain.Waitlist;
import roomescape.repository.WaitlistRepository;

public class FailingWaitlistRepository implements WaitlistRepository {
    private final WaitlistRepository delegate;
    private Long failureDeleteId;

    public FailingWaitlistRepository(WaitlistRepository delegate) {
        this.delegate = delegate;
    }

    public void failOnDelete(Long id) {
        this.failureDeleteId = id;
    }

    @Override
    public Optional<Waitlist> findById(Long id) {
        return delegate.findById(id);
    }

    @Override
    public boolean existsBySameUser(Reservation reservation) {
        return delegate.existsBySameUser(reservation);
    }

    @Override
    public Long save(Reservation reservation, LocalDateTime createdAt) {
        return delegate.save(reservation, createdAt);
    }

    @Override
    public void deleteById(Long id) {
        if (failureDeleteId != null && failureDeleteId.equals(id)) {
            throw new RuntimeException("대기 삭제 실패");
        }

        delegate.deleteById(id);
    }

    @Override
    public List<Waitlist> findByName(String name) {
        return delegate.findByName(name);
    }

    @Override
    public List<Waitlist> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return delegate.findBySlot(date, timeId, themeId);
    }
}
