package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        validateMember(member);
        validateTheme(theme);
        validateDate(date);
        validateTime(time);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = ReservationStatus.RESERVED;
    }

    public Reservation() {
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("사용자를 입력해야 합니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해야 합니다.");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간을 입력해야 합니다.");
        }
    }

    public boolean isBefore(final LocalDateTime other) {
        if (date.isBefore(other.toLocalDate())) {
            return true;
        }
        if (date.equals(other.toLocalDate())) {
            return time.isBefore(other.toLocalTime());
        }
        return false;
    }

    public boolean isSameTime(final ReservationTime other) {
        return time.equals(other);
    }

    public boolean isMemberHasSameId(final long other) {
        return member.hasSameId(other);
    }

    public boolean isThemeHasSameId(final long other) {
        return theme.hasSameId(other);
    }

    public boolean isBetween(final LocalDate from, final LocalDate to) {
        return (date.isAfter(from) || date.isEqual(from)) && (date.isBefore(to) || date.isEqual(to));
    }

    public Member getMember() {
        return member;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return member.getName();
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
