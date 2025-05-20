package roomescape.reservation.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservation.infrastructure.StatusConverter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    @Convert(converter = StatusConverter.class)
    private Status status;

    protected Reservation() {
    }

    private Reservation(final Long id,
                        final LocalDate date,
                        final ReservationTime time,
                        final Theme theme,
                        final Member member,
                        final Status status
    ) {
        validateIsNonNull(date);
        validateIsNonNull(time);
        validateIsNonNull(theme);
        validateIsNonNull(member);
        validateIsNonNull(status);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        setMember(member);
    }

    private void setMember(Member member) {
        this.member = member;
        member.getReservations().add(this);
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("예약 정보는 null 일 수 없습니다.");
        }
    }

    public static Reservation createWithoutId(final LocalDate date,
                                              final ReservationTime time,
                                              final Theme theme,
                                              final Member member,
                                              final Status status
    ) {
        return new Reservation(null, date, time, theme, member, status);
    }

    public boolean isCannotReserveDateTime(final LocalDateTime dateTime) {
        if (date.isBefore(dateTime.toLocalDate())) {
            return true;
        }

        return date.isEqual(dateTime.toLocalDate()) && time.isBefore(dateTime.toLocalTime());
    }

    public boolean isSameTime(final ReservationTime time) {
        return this.time.isEqual(time.getStartAt());
    }

    public Long getId() {
        return id;
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

    public Member getMember() {
        return member;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Reservation that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
