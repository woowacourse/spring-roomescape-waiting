package roomescape.domain.reservation;

import java.util.List;

public class Reservations {
    private final List<Reservation> values;

    public Reservations(List<Reservation> values) {
        this.values = List.copyOf(values);
    }

    public Reservation firstWaiting() {
        return values.stream()
                .filter(Reservation::isWaiting)
                .findFirst()
                .orElseThrow();
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
