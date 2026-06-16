package roomescape.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import roomescape.exception.EntityNotFoundException;

@Getter
public class ReservationEntries {

    private final List<ReservationEntry> entries;

    public ReservationEntries(List<ReservationEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public ReservationEntry addReserved(String name, LocalDateTime createdAt) {
        ReservationEntry entry = ReservationEntry.reserve(name, createdAt);
        entries.add(entry);
        return entry;
    }

    public ReservationEntry addWaiting(String name, LocalDateTime createdAt) {
        ReservationEntry entry = ReservationEntry.waiting(name, createdAt);
        entries.add(entry);
        return entry;
    }

    public ReservationEntry addPending(String name, LocalDateTime createdAt) {
        ReservationEntry entry = ReservationEntry.pending(name, createdAt);
        entries.add(entry);
        return entry;
    }

    public boolean hasActiveEntryByName(String name) {
        return entries.stream()
                .filter(ReservationEntry::isActive)
                .anyMatch(entry -> entry.hasSameName(name));
    }

    public boolean hasReservedEntry() {
        return entries.stream()
                .anyMatch(e -> e.isReserved() || e.isPending());
    }

    public List<ReservationEntry> getEntries() {
        return List.copyOf(entries);
    }

    public Optional<ReservationEntry> findById(long id) {
        return entries.stream()
                .filter(entry -> entry.isSameId(id))
                .findFirst();
    }

    public Optional<ReservationEntry> findByNameAndStatus(String name, ReservationStatus status) {
        return entries.stream()
                .filter(e -> e.matches(name, status))
                .findFirst();
    }

    public void cancel(long id) {
        ReservationEntry entry = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

        replace(id, entry.cancel());
    }

    private void replace(long id, ReservationEntry replacement) {
        entries.replaceAll(e -> e.isSameId(id) ? replacement : e);
    }

    public void promoteFirstWaiting() {
        entries.stream()
                .filter(ReservationEntry::isWaiting)
                .min(Comparator.comparing(ReservationEntry::getCreatedAt)
                        .thenComparing(ReservationEntry::getId))
                .ifPresent(entry -> replace(entry.getId(), entry.promote()));
    }
}
