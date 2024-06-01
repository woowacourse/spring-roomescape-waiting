package roomescape.reservation.domain;

public enum Status {

    CONFIRMED,
    PENDING;

    public static Status fromRank(int rank) {
        if (rank > 0) {
            return Status.PENDING;
        }
        return Status.CONFIRMED;
    }
}
