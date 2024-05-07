package roomescape.repository.dto;

import roomescape.model.Reservation;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationRowDto {

    private final long id;
    private final LocalDate date;
    private final long timeId;
    private final long themeId;
    private final long memberId;

    public ReservationRowDto(long id, LocalDate date, long timeId, long themeId, long memberId) {
        this.id = id;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.memberId = memberId;
    }

    public ReservationRowDto(LocalDate date, long timeId, long themeId, long memberId) {
        this.id = 0;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.memberId = memberId;
    }

    public static ReservationRowDto from(Reservation reservation) {
        return new ReservationRowDto(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId());
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }

    public long getThemeId() {
        return themeId;
    }

    public long getMemberId() {
        return memberId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationRowDto that = (ReservationRowDto) o;
        return id == that.id && timeId == that.timeId && themeId == that.themeId && memberId == that.memberId && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, timeId, themeId, memberId);
    }

    @Override
    public String toString() {
        return "ReservationSavedDto{" +
                "id=" + id +
                ", date=" + date +
                ", timeId=" + timeId +
                ", themeId=" + themeId +
                ", memberId=" + memberId +
                '}';
    }
}
