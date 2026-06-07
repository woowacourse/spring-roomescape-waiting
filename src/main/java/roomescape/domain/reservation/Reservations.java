package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public class Reservations {
    private final List<Reservation> values;

    public Reservations(List<Reservation> values) {
        this.values = List.copyOf(values);
    }

    public Reservation join(String name, Slot slot) {
        Status status = values.stream().anyMatch(Reservation::isApproved) ? Status.WAITING : Status.APPROVED;
        return Reservation.create(name, status, slot);
    }

    public Reservations excluding(Long id) {
        return new Reservations(values.stream().filter(r -> !r.getId().equals(id)).toList());
    }

    public boolean hasByName(String name) {
        return values.stream().anyMatch(r -> r.isSameName(name));
    }

    public Optional<Reservation> firstWaiting() {
        return values.stream()
                .filter(Reservation::isWaiting)
                .findFirst();
    }

    public Rank rankOf(Reservation target) {
        List<Reservation> waitings = values.stream()
                .filter(Reservation::isWaiting)
                .toList();
        int position = waitings.indexOf(target);

        if (position == -1) {
            throw new IllegalStateException("해당 예약이 슬롯 목록에 존재하지 않습니다.");
        }
        return new Rank(position + 1);
    }

    public List<Reservation> getValues() {
        return List.copyOf(values);
    }
}
