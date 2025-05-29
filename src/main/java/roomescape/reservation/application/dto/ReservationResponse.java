package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.user.application.dto.UserResponse;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ReservationResponse(Long reservationId,
                                  UserResponse user,
                                  LocalDate date,
                                  LocalTime time,
                                  String status,
                                  ThemeResponse theme) {

    public ReservationResponse {
        validate(reservationId, user, date, time, status, theme);
    }

    public static ReservationResponse from(final Reservation domain, final User user) {
        return new ReservationResponse(
                domain.getId().getValue(),
                UserResponse.from(user),
                domain.getDate().getValue(),
                domain.getTime().getValue(),
                domain.getStatus().getDescription(),
                ThemeResponse.from(domain.getTheme()));
    }

    public static List<ReservationResponse> from(final List<Reservation> domains, final User user) {
        return domains.stream()
                .map(domain -> ReservationResponse.from(domain, user))
                .toList();
    }

    public static List<ReservationResponse> from(final List<Reservation> domains, final List<User> users) {
        final Map<UserId, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return domains.stream()
                .map(reservation -> {
                    final UserId userId = reservation.getUserId();
                    final User user = userMap.get(userId);
                    return ReservationResponse.from(reservation, user);
                })
                .toList();
    }

    private void validate(final Long reservationId,
                          final UserResponse user,
                          final LocalDate date,
                          final LocalTime time,
                          final String status,
                          final ThemeResponse theme
    ) {
        Validator.of(ReservationResponse.class)
                .validateNotNull(Fields.reservationId, reservationId, DomainTerm.RESERVATION_ID.label())
                .validateNotNull(Fields.user, user, DomainTerm.USER.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.status, status, DomainTerm.BOOKED_STATUS.label())
                .validateNotNull(Fields.theme, theme, DomainTerm.THEME_ID.label());
    }
}
