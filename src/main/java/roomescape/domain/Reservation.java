package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.exception.BadRequestException;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    private RoomTheme theme;

    protected Reservation() {
    }

    public Reservation(Member member, LocalDate date, ReservationTime time, RoomTheme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, RoomTheme theme) {
        validateMember(member);
        validateDate(date);
        validateReservationTime(time);
        validateRoomTheme(theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateRoomTheme(RoomTheme theme) {
        if (theme == null) {
            throw new BadRequestException("테마에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateReservationTime(ReservationTime time) {
        if (time == null) {
            throw new BadRequestException("시간에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("날짜에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new BadRequestException("사용자에 빈값을 입력할 수 없습니다.");
        }
    }

    public boolean hasDateTime(LocalDate date, ReservationTime reservationTime) {
        return this.date.equals(date)
                && this.time.getStartAt().equals(reservationTime.getStartAt());
    }

    public void validateDuplicatedDateTime(Reservation reservation) {
        if (date.equals(reservation.date)
                && time.equals(reservation.time)
                && theme.equals(reservation.theme)) {
            throw new BadRequestException("중복된 시간과 날짜에 대한 예약을 생성할 수 없습니다.");
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

    public ReservationTime getTime() {
        return time;
    }

    public RoomTheme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
