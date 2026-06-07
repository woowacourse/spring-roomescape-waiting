package roomescape.domain.waiting;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

public class Waitings {

    private final List<Waiting> waitings;

    private Waitings(List<Waiting> waitings) {
        this.waitings = List.copyOf(waitings);
    }

    public static Waitings of(List<Waiting> waitings) {
        return new Waitings(waitings);
    }

    public Optional<Waiting> first() {
        return waitings.stream().findFirst();
    }

    public int positionOf(String name) {
        return IntStream.range(0, waitings.size())
                .filter(i -> waitings.get(i).getName().equals(name))
                .map(i -> i + 1)
                .findFirst()
                .orElseThrow(() -> new RoomescapeException(ErrorCode.WAITING_ID_NOT_FOUND));
    }

    public void validateCanEnqueue(String name, Reservation reservation) {
        if (waitings.stream().anyMatch(w -> w.getName().equals(name))) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_WAITING_NAME);
        }
        if (reservation.isOwner(name)) {
            throw new RoomescapeException(ErrorCode.WAITING_NOT_AVAILABLE);
        }
    }
}