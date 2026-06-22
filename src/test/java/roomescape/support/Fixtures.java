package roomescape.support;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

/**
 * 도메인 객체 생성을 한 줄로 줄여주는 픽스처.
 *
 * <p>단위 테스트에서 매번 ReservationTime.withId(...), Theme.withId(...)를 길게 쓰면
 * 테스트의 의도(무엇을 검증하는가)가 객체 조립 코드에 묻힌다. 자주 쓰는 기본값을 여기 모은다.
 *
 * <p>주의: 이건 "도메인 단위 테스트"용 픽스처다. DB에 넣는 건 ReservationTestHelper가 담당한다.
 * 둘을 섞지 않는다 — 하나는 메모리 객체, 하나는 영속 데이터.
 */
public final class Fixtures {

    public static final LocalDate FUTURE_DATE = LocalDate.of(2050, 12, 31);
    public static final LocalTime TEN = LocalTime.of(10, 0);

    private Fixtures() {
    }

    public static ReservationTime time(long id, LocalTime startAt) {
        return ReservationTime.withId(id, startAt);
    }

    public static ReservationTime time(long id) {
        return ReservationTime.withId(id, TEN);
    }

    public static Theme theme(long id) {
        return Theme.withId(id, "테마" + id, "설명", "https://example.com/" + id + ".jpg");
    }

    public static Theme theme(long id, String name) {
        return Theme.withId(id, name, "설명", "https://example.com/" + id + ".jpg");
    }

    public static Reservation reservation(long id, String name) {
        return reservation(id, name, ReservationStatus.CONFIRMED);
    }

    public static Reservation reservation(long id, String name, ReservationStatus status) {
        // withId 경로는 정책 검증을 거치지 않는다(이미 저장된 것을 복원하는 용도).
        return Reservation.withId(id, name, FUTURE_DATE, time(1), theme(1), status);
    }

    public static Waiting waiting(long id, String name, int order) {
        return Waiting.withId(id, name, FUTURE_DATE, time(1), theme(1), order);
    }

    public static Waiting waiting(long id, String name, LocalDate date,
                                  ReservationTime time, Theme theme, int order) {
        return Waiting.withId(id, name, date, time, theme, order);
    }
}
