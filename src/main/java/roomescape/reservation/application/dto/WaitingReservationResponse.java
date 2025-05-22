package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.user.domain.User;
import roomescape.user.ui.dto.UserResponse;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record WaitingReservationResponse(Long waitingReservationId,
                                         UserResponse user,
                                         int waitingNumber) {

    public WaitingReservationResponse {
        validate(waitingReservationId, user);
    }

    public static WaitingReservationResponse from(final WaitingReservation domain, final User user) {
        return new WaitingReservationResponse(
                domain.getId(),
                UserResponse.from(user),
                domain.getWaitingOrder());
    }

    private void validate(final Long id,
                          final UserResponse user) {
        Validator.of(WaitingReservationResponse.class)
                .validateNotNull(Fields.waitingReservationId, id, DomainTerm.RESERVATION_WAITING_Id.label())
                .validateNotNull(Fields.user, user, DomainTerm.USER.label());
    }
}
