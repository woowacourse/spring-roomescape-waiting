package roomescape.waiting.domain;

public record Rank(long value) {
    public Rank {
        if (value <= 0) {
            throw new IllegalArgumentException("순위는 0 이하가 될 수 없습니다.");
        }
    }
}
