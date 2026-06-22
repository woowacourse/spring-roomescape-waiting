package roomescape.domain.reservation;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Reservations {
    private final List<Reservation> values;

    public Reservations(List<Reservation> values) {
        this.values = List.copyOf(values);
    }

    public Reservation join(Reservation assembled) {
        conflictByMember(assembled);
        return assembled.withStatus(nextStatus());
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

    public List<ReservationWithRank> withRanks(Map<Long, Long> rankMap) {
        return values.stream()
                .map(r -> new ReservationWithRank(r, rankMap.getOrDefault(r.getId(), 0L)))
                .toList();
    }

    public List<Reservation> getValues() {
        return List.copyOf(values);
    }
}
