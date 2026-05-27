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

    public void addReserved(String name, Reservation reservation) {
        entries.add(ReservationEntry.reserve(name, reservation));
    }

    public void addWaiting(String name, Reservation reservation) {
        entries.add(ReservationEntry.waiting(name, reservation));
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

    public void promoteFirstWaiting() {
        entries.stream()
                .filter(ReservationEntry::isWaiting)
                .min(Comparator.comparing(ReservationEntry::getCreatedAt))
                .ifPresent(ReservationEntry::promote);
    }
}
