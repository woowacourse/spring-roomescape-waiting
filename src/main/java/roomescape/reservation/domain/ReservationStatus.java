package roomescape.reservation.domain;

import java.util.Map;
import java.util.Set;

public enum ReservationStatus {
  RESERVED,
  WAITING,
  CANCELED;

  private static final Map<ReservationStatus, Set<ReservationStatus>> TRANSITIONS = Map.of(
      RESERVED, Set.of(CANCELED),
      WAITING, Set.of(RESERVED, CANCELED),
      CANCELED, Set.of(RESERVED)
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
