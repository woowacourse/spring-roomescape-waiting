package roomescape.domain.waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.util.DateUtil;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    protected Waiting() {
    }

    public Waiting(Member member, Reservation reservation) {
        validateIsReservationOwner(member, reservation);
        validateDateTime(reservation);

        this.member = member;
        this.reservation = reservation;
    }

    private void validateDateTime(Reservation reservation) {
        LocalDate date = reservation.getDate();
        LocalTime time = reservation.getTime().getStartAt();
        if (DateUtil.isPastDateTime(date, time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 지나간 날짜와 시간은 예약 대기가 불가능합니다.",
                    new Throwable("생성 예약 대기 시간 : " + date + " " + time)
            );
        }
    }

    private void validateIsReservationOwner(Member member, Reservation reservation) {
        if (reservation.isOwner(member)) {
            throw new IllegalArgumentException(
                    "[ERROR] 자신의 예약에 대한 예약 대기를 생성할 수 없습니다.",
                    new Throwable("reservation_id : " + reservation.getId())
            );
        }
    }

    public void approve() {
        reservation.changeOwner(member);
    }

    public boolean isNotOwner(Member member) {
        return !this.member.equals(member);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
