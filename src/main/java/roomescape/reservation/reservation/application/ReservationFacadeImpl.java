package roomescape.reservation.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.aop.ForbiddenException;
import roomescape.auth.session.UserSession;
import roomescape.reservation.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.reservation.application.dto.ReservationResponse;
import roomescape.reservation.reservation.application.dto.ReservationSearchRequest;
import roomescape.reservation.reservation.application.service.ReservationCommandService;
import roomescape.reservation.reservation.application.service.ReservationQueryService;
import roomescape.reservation.reservation.domain.Reservation;
import roomescape.reservation.reservation.domain.ReservationId;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationFacadeImpl implements ReservationFacade {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final UserQueryService userQueryService;

    @Override
    public List<ReservationResponse> getAll() {
        final List<Reservation> reservations = reservationQueryService.getAll();
        final List<UserId> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .toList();

        final List<User> users = userQueryService.getAllByIds(userIds);
        return ReservationResponse.from(reservations, users);
    }

    @Override
    public List<ReservationResponse> getByParams(final ReservationSearchRequest request) {
        final List<Reservation> reservations = reservationQueryService.getByParams(request);
        final List<UserId> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .toList();

        final List<User> users = userQueryService.getAllByIds(userIds);

        return ReservationResponse.from(reservations, users);
    }

    @Override
    public List<ReservationResponse> getAllByUserId(final UserId id) {
        final User user = userQueryService.getById(id);
        return ReservationResponse.from(
                reservationQueryService.getAllByUserId(id), user);
    }

    @Override
    public ReservationResponse create(final CreateReservationRequest request, final UserSession userSession) {
        final Reservation reservation = reservationCommandService.create(request);

        final User user = userQueryService.getById(reservation.getUserId());
        return ReservationResponse.from(reservation, user);
    }

    @Override
    public void delete(final ReservationId id, final UserSession userSession) {
        final Reservation target = reservationQueryService.getById(id);
        if (userSession.canManage(target.getUserId())) {
            reservationCommandService.delete(id);
            return;
        }
        throw new ForbiddenException(userSession.id(), userSession.role(), target.getUserId());
    }
}
