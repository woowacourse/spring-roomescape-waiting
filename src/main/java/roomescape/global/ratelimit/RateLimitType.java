package roomescape.global.ratelimit;

/**
 * 레이트 리밋의 종류와 종류별 기본 한도를 정의한다.
 * 들어오는(INBOUND)·나가는(OUTBOUND) 호출은 서로 독립된 버킷·한도를 가진다.
 *
 * 여기 선언된 값은 외부 설정(rate-limit.limits.&lt;type&gt;.*)이 없을 때 적용되는 기본값이다.
 * 실제 운영 한도는 설정으로 주입하고, 이 값은 안전한 폴백 역할을 한다.
 */
public enum RateLimitType {

    INBOUND(10, 100.0),
    OUTBOUND(5, 100.0);

    private final int defaultCapacity;
    private final double defaultRefillPerSecond;

    RateLimitType(int defaultCapacity, double defaultRefillPerSecond) {
        this.defaultCapacity = defaultCapacity;
        this.defaultRefillPerSecond = defaultRefillPerSecond;
    }

    public int defaultCapacity() {
        return defaultCapacity;
    }

    public double defaultRefillPerSecond() {
        return defaultRefillPerSecond;
    }
}
