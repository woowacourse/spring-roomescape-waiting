package roomescape.domain.reservation;

public class Rank {
    private long value;

    protected Rank(){
    }

    public Rank(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
