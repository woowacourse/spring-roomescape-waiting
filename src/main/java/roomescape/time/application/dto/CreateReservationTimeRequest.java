package roomescape.time.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.TimeValue;

import java.time.LocalTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationTimeRequest(TimeValue startAt) {

    public CreateReservationTimeRequest {
        validate(startAt);
    }

    public ReservationTime toDomain() {
        return ReservationTime.withoutId(startAt);
    }

    private void validate(final TimeValue startAt) {
        Validator.of(CreateReservationTimeRequest.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
