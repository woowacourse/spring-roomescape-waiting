package roomescape.domain;

import java.time.LocalDate;
import roomescape.domain.exception.InvalidDomainException;
import roomescape.domain.policy.ReservationPolicy;

public class Reservation {
    private static final int MAX_NAME_LENGTH = 30;

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;

    private Reservation(Long id, String name, LocalDate date,
                        ReservationTime time, Theme theme, ReservationStatus status) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation create(String name, LocalDate date,
                                     ReservationTime time, Theme theme,
                                     ReservationPolicy policy) {
        policy.validateCreatable(ReservationDateTime.of(date, time.getStartAt()));
        // step3: 기존 동작 보존(CONFIRMED). step4에서 결제 게이트 도입 시 PENDING으로 전환.
        return new Reservation(null, name, date, time, theme, ReservationStatus.CONFIRMED);
    }

    public static Reservation createPending(String name, LocalDate date,
                                            ReservationTime time, Theme theme,
                                            ReservationPolicy policy) {
        policy.validateCreatable(ReservationDateTime.of(date, time.getStartAt()));
        return new Reservation(null, name, date, time, theme, ReservationStatus.PENDING);
    }

    public static Reservation withId(Long id, String name, LocalDate date,
                                     ReservationTime time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, date, time, theme, status);
    }

    public static Reservation promote(Waiting w) {
        // [이월] 대기 승격 시 결제 흐름은 이번 미션 범위 밖 → 즉시 확정으로 둔다.
        return new Reservation(null, w.getName(), w.getDate(), w.getTime(), w.getTheme(),
                ReservationStatus.CONFIRMED);
    }

    public ReservationDateTime dateTime() {
        return ReservationDateTime.of(date, time.getStartAt());
    }

    public ReservationDateTime dateTime() {
        return ReservationDateTime.of(date, time.getStartAt());
    }

    private static void validate(String name, LocalDate date, ReservationTime time, Theme theme) {
        validateName(name);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                    "예약자 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new InvalidDomainException("예약 날짜는 비어 있을 수 없습니다.");
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new InvalidDomainException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new InvalidDomainException("예약 테마는 비어 있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }
}
