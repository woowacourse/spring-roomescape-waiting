package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(final Member member, final String date, final ReservationTime time, final Theme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(final Long id, final Member member, final String date,
                       final ReservationTime time, final Theme theme) {
        this(id, member, convertToLocalDate(date), time, theme);
    }

    public Reservation(final Long id, final Member member, final LocalDate date,
                       final ReservationTime time, final Theme theme) {
        validateDate(date);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = ReservationStatus.WAITING;
    }

    private static LocalDate convertToLocalDate(final String date) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("예약 날짜가 비어있습니다.");
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("유효하지 않은 예약 날짜입니다.");
        }
    }

    private void validateDate(final LocalDate date) {
        if (date.isBefore(LocalDate.now()) || date.equals(LocalDate.now())) {
            throw new IllegalArgumentException("이전 날짜 혹은 당일은 예약할 수 없습니다.");
        }
    }

    public boolean hasSameDateTime(final LocalDate date, final ReservationTime time) {
        return this.time.equals(time) && this.date.equals(date);
    }

    public void toReserved() {
        this.status = ReservationStatus.RESERVED;
    }

    public Long getReservationTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public String getThemeName() {
        return theme.getName();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getMemberName() {
        return member.getNameString();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public String getStatus() {
        return status.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
