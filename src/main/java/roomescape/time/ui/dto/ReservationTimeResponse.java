package roomescape.time.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.time.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ReservationTimeResponse(Long id,
                                      LocalTime startAt) {

    public ReservationTimeResponse {
        validate(id, startAt);
    }

    public static ReservationTimeResponse from(final ReservationTime domain) {
        return new ReservationTimeResponse(
                domain.getId().getValue(),
                domain.getStartAt());
    }

    public static List<ReservationTimeResponse> from(final List<ReservationTime> domains) {
        return domains.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    private void validate(final Long id, final LocalTime startAt) {
        Validator.of(ReservationTimeResponse.class)
                .validateNotNull(ReservationTimeResponse.Fields.id, id, DomainTerm.RESERVATION_TIME_ID.label())
                .validateNotNull(ReservationTimeResponse.Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }
}
