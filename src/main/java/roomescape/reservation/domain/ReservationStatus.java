package roomescape.reservation.domain;

import java.util.Map;
import java.util.Set;

public enum ReservationStatus {
  PENDING,
  CONFIRMED,
  RESERVED,
  WAITING,
  CANCELED;

  private static final Map<ReservationStatus, Set<ReservationStatus>> TRANSITIONS = Map.of(
      PENDING, Set.of(CONFIRMED),
      RESERVED, Set.of(CANCELED),
      WAITING, Set.of(RESERVED, CANCELED)
  );

  public boolean canTransitionTo(ReservationStatus status) {
    return TRANSITIONS.getOrDefault(this, Set.of()).contains(status);
  }

  public ReservationStatus transitionTo(ReservationStatus status) {
    if (!canTransitionTo(status)) {
      throw new IllegalStateException(
          this + "에서 " + status + "로 전이할 수 없습니다."
      );
    }
    return status;
  }
}
