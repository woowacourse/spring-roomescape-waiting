package roomescape.reservation.domain;

public record ReservationEntry(
        Reservation reservation,
        ReservationStatus status,
        Long waitingRank
) {
    private static final Long RESERVED_WAITING_RANK = 0L;
    private static final Long NO_WAITING_RANK = null;
    private static final long FIRST_WAITING_RANK = 1L;

    public ReservationEntry {
        validate(reservation, status, waitingRank);
    }

    public static ReservationEntry reserved(Reservation reservation) {
        return new ReservationEntry(reservation, ReservationStatus.RESERVED, RESERVED_WAITING_RANK);
    }

    public static ReservationEntry waiting(Reservation reservation, long waitingRank) {
        return new ReservationEntry(reservation, ReservationStatus.WAITING, waitingRank);
    }

    public static ReservationEntry canceled(Reservation reservation) {
        return new ReservationEntry(reservation, ReservationStatus.CANCELED, NO_WAITING_RANK);
    }

    private static void validate(Reservation reservation, ReservationStatus status, Long waitingRank) {
        validateReservation(reservation);
        validateStatus(status);
        validateWaitingRank(status, waitingRank);
    }

    private static void validateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("예약 정보는 비어 있을 수 없습니다.");
        }
    }

    private static void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 비어 있을 수 없습니다.");
        }
    }

    private static void validateWaitingRank(ReservationStatus status, Long waitingRank) {
        if (status == ReservationStatus.RESERVED && !RESERVED_WAITING_RANK.equals(waitingRank)) {
            throw new IllegalArgumentException("예약 확정 상태의 대기 순번은 " + RESERVED_WAITING_RANK + "이어야 합니다.");
        }
        if (status == ReservationStatus.WAITING && (waitingRank == null || waitingRank < FIRST_WAITING_RANK)) {
            throw new IllegalArgumentException("대기 상태의 대기 순번은 " + FIRST_WAITING_RANK + " 이상이어야 합니다.");
        }
        if (status == ReservationStatus.CANCELED && waitingRank != null) {
            throw new IllegalArgumentException("취소 상태의 대기 순번은 비어 있어야 합니다.");
        }
    }
}
