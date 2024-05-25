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

    public Waiting(Member member, Slot slot) {
        this(null, member, slot);
    }

    public Waiting(Member member, LocalDate date, ReservationTime reservationTime,
                   Theme theme) {
        this(null, member, date, reservationTime, theme);
    }

    public Waiting(Long id,
                   Member member,
                   Slot slot) {
        validateReservationMemberIsNull(member);
        validateReservationDateIsNull(slot.date());
        validateReservationTimeIsNull(slot.reservationTime());
        validateReservationThemeIsNull(slot.theme());

        this.id = id;
        this.member = member;
        this.slot = slot;
    }

    public Waiting(Long id,
                   Member member,
                   LocalDate date,
                   ReservationTime reservationTime,
                   Theme theme) {
        this(id, member, new Slot(date, reservationTime, theme));
    }

    protected Waiting() {
    }

    public static Waiting create(Member member, Slot slot) {
        validateCreateTimeIsPast(slot.date(), slot.reservationTime());
        return new Waiting(null, member, slot);
    }

    public static Waiting create(Member member,
                                 LocalDate date,
                                 ReservationTime reservationTime,
                                 Theme theme) {
        return create(member, new Slot(date, reservationTime, theme));
    }

    private static void validateCreateTimeIsPast(LocalDate date, ReservationTime reservationTime) {
        checkDateTimeIsPast(date, reservationTime.getStartAt());
    }

    private static void checkDateTimeIsPast(LocalDate date, LocalTime startAt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = LocalDateTime.of(date, startAt);
        if (dateTime.isBefore(now)) {
            throw new IllegalArgumentException(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"))
                    + "는 지나간 시간임으로 예약 대기 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }
    }

    private void validateReservationMemberIsNull(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기자는 필수입니다.");
        }
    }

    private void validateReservationDateIsNull(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기 날짜는 필수입니다.");
        }
    }

    private void validateReservationTimeIsNull(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기 시간은 필수입니다.");
        }
    }

    private void validateReservationThemeIsNull(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("예약 대기 생성 시 예약 대기 테마는 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getReservationTime() {
        return slot.reservationTime();
    }

    public Theme getTheme() {
        return slot.theme();
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.date();
    }

    public String getThemeName() {
        return slot.getThemeName();
    }

    public LocalTime getStartAt() {
        return slot.getStartAt();
    }

    @Override
    public boolean equals(Object o) {
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
