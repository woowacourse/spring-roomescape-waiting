package roomescape.global.ratelimit;

@FunctionalInterface
public interface NanoClock {

    long currentNanoseconds();
}
