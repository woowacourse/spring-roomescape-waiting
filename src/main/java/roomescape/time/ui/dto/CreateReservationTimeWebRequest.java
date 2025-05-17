package roomescape.time.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

import java.time.LocalTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationTimeWebRequest(LocalTime startAt) {

    public CreateReservationTimeWebRequest {
        validate(startAt);
    }

    private void validate(final LocalTime startAt) {
        Validator.of(CreateReservationTimeWebRequest.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
