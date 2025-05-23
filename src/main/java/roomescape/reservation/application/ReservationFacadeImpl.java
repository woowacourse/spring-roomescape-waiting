package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.application.dto.WaitingReservationResponse;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.ui.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.ReservationSearchWebRequest;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationFacadeImpl implements ReservationFacade {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final UserQueryService userQueryService;

    @Override
    public List<ReservationResponse> getAll() {
        final List<Reservation> reservations = reservationQueryService.getAll();
        final List<Long> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .toList();

        final List<User> users = userQueryService.getAllByIds(userIds);
        return ReservationResponse.from(reservations, users);
    }

    @Override
    public List<AvailableReservationTimeWebResponse> getAvailable(final LocalDate date, final Long themeId) {
        final AvailableReservationTimeServiceRequest request = new AvailableReservationTimeServiceRequest(
                ReservationDate.from(date),
                themeId);

        return reservationQueryService.getTimesWithAvailability(request).stream()
                .map(AvailableReservationTimeWebResponse::from)
                .toList();
    }

    @Override
    public List<ReservationResponse> getByParams(final ReservationSearchWebRequest request) {
        final List<Reservation> reservations = reservationQueryService.getByParams(request.toServiceRequest());
        final List<Long> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .toList();

        final List<User> users = userQueryService.getAllByIds(userIds);

        return ReservationResponse.from(reservations, users);
    }

    @Override
    public List<ReservationResponse> getAllByUserId(final Long userId) {
        final User user = userQueryService.getById(userId);
        return ReservationResponse.from(reservationQueryService.getAllByUserId(userId), user);
    }

    @Override
    @Transactional
    public ReservationResponse create(final CreateReservationWithUserIdWebRequest request) {
        final User user = userQueryService.getById(request.userId());

        final Reservation reservation = reservationCommandService.create(
                request.toServiceRequest());

        return ReservationResponse.from(reservation, user);
    }

    @Override
    public void delete(final Long id) {
        reservationCommandService.delete(id);
    }

    @Override
    public WaitingReservationResponse addWaiting(final CreateReservationWithUserIdWebRequest request) {
        final User user = userQueryService.getById(request.userId());

        final WaitingReservation waitingReservation = reservationCommandService.createWaitingReservation(
                request.toServiceRequest());

        return WaitingReservationResponse.from(waitingReservation, user);
    }

    @Override
    public void deleteWaiting(final Long id) {
        reservationCommandService.deleteWaiting(id);
    }
}
