package roomescape.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @Valid
    @NotNull
    @Embedded
    private ReservationInfo reservationInfo;

    private Waiting(Long id, Member member, ReservationInfo reservationInfo) {
        this.id = id;
        this.member = member;
        this.reservationInfo = reservationInfo;
    }

    public Waiting(LocalDate date, ReservationTime time, Theme theme, Member member) { // TODO: 파라미터 순서 변경
        this(0L, member, new ReservationInfo(date, time, theme));
    }

    public static Waiting of(ReservationDto reservationDto, ReservationTime time, Theme theme, Member member) {
        return new Waiting(0L, member, new ReservationInfo(reservationDto.getDate(), time, theme));
    } // TODO: to dto

    protected Waiting() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return reservationInfo.getDate();
    }

    public ReservationTime getTime() {
        return reservationInfo.getTime();
    }

    public Theme getTheme() {
        return reservationInfo.getTheme();
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting waiting = (Waiting) o;
        return id.equals(waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationInfo, member);
    }
}
