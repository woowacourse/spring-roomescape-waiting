package roomescape.domain.reservation;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

import java.util.List;
import java.util.Optional;

public class Reservations {
    private final List<Reservation> values;

    public Reservations(List<Reservation> values) {
        this.values = List.copyOf(values);
    }

    public Reservation join(Reservation assembled) {
        conflictByMember(assembled);
        Reservation withStatus = assembled.withStatus(nextStatus());
        return withStatus.withRank(rankOf(withStatus));
    }

    private Status nextStatus() {
        return values.stream().anyMatch(Reservation::isApproved) ? Status.WAITING : Status.APPROVED;
    }

    public void conflictByMember(Reservation reservation) {
        if (hasByMember(reservation)) {
            throw new RoomEscapeException(DomainErrorCode.ALREADY_EXISTS, "이미 같은 슬롯에 예약이 존재합니다: " + reservation.getMember().getName());
        }
    }

    public boolean hasByMember(Reservation other) {
        return values.stream()
                .anyMatch(r -> r.isSameMember(other));
    }

    public Reservations excluding(Long id) {
        return new Reservations(values.stream().filter(r -> !r.getId().equals(id)).toList());
    }

    public Optional<Reservation> firstWaiting() {
        return values.stream()
                .filter(Reservation::isWaiting)
                .findFirst();
    }

    public Rank rankOf(Reservation reservation) {
        if (reservation.isApproved()) {
            return new Rank(0);
        }
        List<Reservation> waitings = values.stream().filter(Reservation::isWaiting).toList();
        int position = waitings.indexOf(reservation);
        return position == -1 ? new Rank(waitings.size() + 1) : new Rank(position + 1);
    }

    public List<Reservation> getValues() {
        return List.copyOf(values);
    }
}
