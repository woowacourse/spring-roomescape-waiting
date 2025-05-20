package roomescape.reservation.time.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.time.application.dto.CreateReservationTimeRequest;

import java.time.LocalTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationTimeWebRequest(LocalTime startAt) {

    public CreateReservationTimeWebRequest {
        validate(startAt);
    }

    public CreateReservationTimeRequest toServiceRequest() {
        return new CreateReservationTimeRequest(startAt);
    }

    private void validate(final LocalTime startAt) {
        Validator.of(CreateReservationTimeWebRequest.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
