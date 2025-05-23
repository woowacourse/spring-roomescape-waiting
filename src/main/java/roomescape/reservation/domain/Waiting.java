package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedDate;
import roomescape.common.exception.BadRequestException;
import roomescape.common.utils.Validator;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @CreatedDate
    private LocalDateTime createdAt;

    private static Waiting of(
            final Long id,
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        validate(member, date, time, theme);
        return new Waiting(id, member, date, time, theme, LocalDateTime.now());
    }

    public static Waiting withId(
            final Long id,
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        return of(id, member, date, time, theme);
    }

    public static Waiting withoutId(
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {

        validatePast(date, time);
        return of(null, member, date, time, theme);
    }

    private static void validate(
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {

        Validator.of(Waiting.class)
                .notNullField(Fields.member, member)
                .notNullField(Fields.date, date)
                .notNullField(Fields.time, time)
                .notNullField(Fields.theme, theme);
    }

    public static void validatePast(final ReservationDate date, final ReservationTime time) {
        final LocalDateTime now = LocalDateTime.now();
        if (date.isAfter(now.toLocalDate())) {
            return;
        }
        if (date.isBefore(now.toLocalDate())) {
            throw new BadRequestException("지난 날짜는 예약할 수 없습니다.");
        }
        if (time.isBefore(now.toLocalTime())) {
            throw new BadRequestException("이미 지난 시간에는 예약할 수 없습니다.");
        }
    }
}
