package roomescape.domain;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static roomescape.domain.exception.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.exception.DomainPreconditions.requireNonNull;

public class Reservation {

    private final Long id;
    private final Member member;
    private final Schedule schedule;
    private final ReservationStatus status;
    private final LocalDateTime updateAt;

    public Reservation(Long id, Member member, Schedule schedule, ReservationStatus status, LocalDateTime updateAt) {
        this.id = id;
        this.member = requireNonNull(member, INVALID_INPUT, "예약 회원은 비어있을 수 없습니다.");
        this.schedule = requireNonNull(schedule, INVALID_INPUT, "예약 스케줄은 비어있을 수 없습니다.");
        this.status = requireNonNull(status, INVALID_INPUT, "예약 상태는 비어있을 수 없습니다.");
        this.updateAt = requireNonNull(updateAt, INVALID_INPUT, "기준 일시는 비어있을 수 없습니다.");
    }

    public static Reservation createBy(
            Member member,
            Schedule schedule,
            ReservationStatus status,
            LocalDateTime now
    ) {
        validateNotPastSchedule(schedule, now);
        return new Reservation(null, member, schedule, status, now);
    }

    public Reservation cancelBy(
            Member member,
            LocalDateTime now
    ) {
        this.member.validateSameMember(member);
        validateCanModify(now);
        return new Reservation(this.id, this.member, schedule, ReservationStatus.CANCELED, now);
    }

    public Reservation cancelByAdmin(LocalDateTime now) {
        validateCanModify(now);
        return new Reservation(this.id, this.member, schedule, ReservationStatus.CANCELED, now);
    }

    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }

    public boolean isAlreadyCanceled() {
        return this.status == ReservationStatus.CANCELED;
    }

    private void validateCanModify(LocalDateTime now) {
        if (isPast(this.schedule, now)) {
            throw new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "이미 지난 예약은 변경 및 취소할 수 없습니다.");
        }
    }

    private static void validateNotPastSchedule(Schedule schedule, LocalDateTime now) {
        requireNonNull(schedule, INVALID_INPUT, "예약 스케줄은 비어있을 수 없습니다.");
        requireNonNull(now, INVALID_INPUT, "기준 일시는 비어있을 수 없습니다.");
        if (isPast(schedule, now)) {
            throw new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "과거 시각으로는 수정 및 예약할 수 없습니다.");
        }
    }

    private static boolean isPast(Schedule schedule, LocalDateTime now) {
        LocalDate date = schedule.getDate();
        ReservationTime time = schedule.getTime();
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return !reservationDateTime.isAfter(now);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }
}
