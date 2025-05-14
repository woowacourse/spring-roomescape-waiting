package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
public final class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Member member;
    @ManyToOne
    private Theme theme;

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        validateNotNull(member, date, time, theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.member = member;
        this.theme = theme;
    }

    public Reservation() {

    }

    public static Reservation register(final Member member, final LocalDate date,
                                       final ReservationTime time, final Theme theme) {
        boolean isBefore = false;
        final LocalDateTime other = LocalDateTime.now();
        if (date.isBefore(other.toLocalDate())) {
            isBefore = true;
        }
        if (date.equals(other.toLocalDate())) {
            isBefore = time.isBeforeOrEqual(other.toLocalTime());
        }
        if (isBefore) {
            throw new BadRequestException("지나간 날짜와 시간은 예약 불가합니다.");
        }
        return new Reservation(null, member, date, time, theme);
    }

    public Reservation withId(final long id) {
        return new Reservation(id, member, date, time, theme);
    }

    private void validateNotNull(final Member member, final LocalDate date, final ReservationTime time,
                                 final Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("사용자를 입력해야 합니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해야 합니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("시간을 입력해야 합니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
