package roomescape.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ReservationEntries {

    private final List<ReservationEntry> entries;

    public ReservationEntries(List<ReservationEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public ReservationEntry addReserved(String name, Reservation reservation) {
        ReservationEntry entry = ReservationEntry.reserve(name, reservation);
        entries.add(entry);
        return entry;
    }

    public ReservationEntry addWaiting(String name, Reservation reservation) {
        ReservationEntry entry = ReservationEntry.waiting(name, reservation);
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
                .anyMatch(ReservationEntry::isReserved);
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

    public void promoteFirstWaiting() {
        entries.stream()
                .filter(ReservationEntry::isWaiting)
                .min(Comparator.comparing(ReservationEntry::getCreatedAt)
                        .thenComparing(ReservationEntry::getId))
                .ifPresent(ReservationEntry::promote);
    }
}
