package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class ReservationInfo {

    @ManyToOne
    private Member member;

    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public static ReservationInfo of(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        validate(member, date, time, theme);
        return new ReservationInfo(member, date, time, theme);
    }

    private static void validate(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        Validator.of(ReservationInfo.class)
                .notNullField(Fields.member, member)
                .notNullField(Fields.date, date)
                .notNullField(Fields.time, time)
                .notNullField(Fields.theme, theme);
    }
}
