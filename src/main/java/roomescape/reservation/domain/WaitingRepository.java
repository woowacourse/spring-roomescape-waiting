package roomescape.reservation.domain;

public interface WaitingRepository {

    boolean exists(Long reservationId, Long memberId);

    Waiting save(Waiting withoutId);
}
