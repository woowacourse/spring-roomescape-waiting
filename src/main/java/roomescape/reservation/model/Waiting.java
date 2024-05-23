package roomescape.reservation.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Embedded
    private Slot slot;

    public Waiting(final Member member, final Slot slot) {
        this(null, member, slot);
    }

    public Waiting(final Member member, final LocalDate date, final ReservationTime reservationTime,
                   final Theme theme) {
        this(null, member, date, reservationTime, theme);
    }

    public Waiting(final Long id,
                   final Member member,
                   final Slot slot) {
        validateReservationMemberIsNull(member);
        validateReservationDateIsNull(slot.date());
        validateReservationTimeIsNull(slot.reservationTime());
        validateReservationThemeIsNull(slot.theme());

        this.id = id;
        this.member = member;
        this.slot = slot;
    }

    public Waiting(final Long id,
                   final Member member,
                   final LocalDate date,
                   final ReservationTime reservationTime,
                   final Theme theme) {
        this(id, member, new Slot(date, reservationTime, theme));
    }

    protected Waiting() {
    }

    public static Waiting create(final Member member, final Slot slot) {
        validateCreateTimeIsPast(slot.date(), slot.reservationTime());
        return new Waiting(null, member, slot);
    }

    public static Waiting create(final Member member,
                                 final LocalDate date,
                                 final ReservationTime reservationTime,
                                 final Theme theme) {
        return create(member, new Slot(date, reservationTime, theme));
    }

    private static void validateCreateTimeIsPast(final LocalDate date, final ReservationTime reservationTime) {
        checkDateTimeIsPast(date, reservationTime.getStartAt());
    }

    private static void checkDateTimeIsPast(final LocalDate date, final LocalTime startAt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = LocalDateTime.of(date, startAt);
        if (dateTime.isBefore(now)) {
            throw new IllegalArgumentException(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"))
                    + "는 지나간 시간임으로 예약 대기 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }
    }

    private void validateReservationMemberIsNull(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기자는 필수입니다.");
        }
    }

    private void validateReservationDateIsNull(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기 날짜는 필수입니다.");
        }
    }

    private void validateReservationTimeIsNull(final ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기 시간은 필수입니다.");
        }
    }

    private void validateReservationThemeIsNull(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기 테마는 필수입니다.");
        }
    }

    public Slot getSlot() {
        return slot;
//        return new Slot(date, reservationTime, theme);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return slot.date();
    }

    public ReservationTime getReservationTime() {
        return slot.reservationTime();
    }

    public Theme getTheme() {
        return slot.theme();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting that = (Waiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
