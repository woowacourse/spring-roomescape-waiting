package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.DuplicateException;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.MyReservationsResponse;
import roomescape.reservation.application.dto.SimpleWaitingReservationResponse;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.application.service.ReservationViewQueryService;
import roomescape.reservation.application.service.WaitingReservationCommandService;
import roomescape.reservation.application.service.WaitingReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.ui.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.ReservationSearchWebRequest;
import roomescape.reservation.ui.dto.WaitingReservationResponse;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationFacadeImpl implements ReservationFacade {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final WaitingReservationCommandService waitingReservationCommandService;
    private final WaitingReservationQueryService waitingReservationQueryService;
    private final ReservationViewQueryService reservationViewQueryService;
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
    public List<MyReservationsResponse> getAllByUserId(final Long userId) {
        userQueryService.getById(userId);
        return reservationViewQueryService.getAllByUserId(userId)
                .stream()
                .map(MyReservationsResponse::from)
                .sorted(Comparator.comparing(MyReservationsResponse::sequence))
                .toList();
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
    @Transactional
    public void delete(final Long id) {
        Optional<Long> waitingId = reservationViewQueryService.findFirstWaitingByReservationId(id);
        waitingId.ifPresentOrElse(
                waiting -> promotionWaiting(id, waiting),
                () -> reservationCommandService.delete(id)
        );
    }

    @Override
    public List<WaitingReservationResponse> getAllWaiting() {
        final List<WaitingReservation> waiting = waitingReservationQueryService.getAll();
        final List<Long> userIds = waiting.stream()
                .map(WaitingReservation::getUserId)
                .toList();

        final List<User> users = userQueryService.getAllByIds(userIds);
        return WaitingReservationResponse.from(waiting, users);
    }

    @Override
    public SimpleWaitingReservationResponse addWaiting(final CreateReservationWithUserIdWebRequest request) {
        final User user = userQueryService.getById(request.userId());
        final CreateReservationServiceRequest serviceRequest = request.toServiceRequest();

        if (reservationViewQueryService.existsByParams(serviceRequest, user.getId())) {
            throw new DuplicateException(DomainTerm.RESERVATION,
                    request.date(),
                    DomainTerm.THEME_ID,
                    DomainTerm.RESERVATION_TIME_ID,
                    DomainTerm.USER_ID
            );
        }

        final WaitingReservation waitingReservation
                = waitingReservationCommandService.create(serviceRequest);

        return SimpleWaitingReservationResponse.from(waitingReservation, user);
    }

    @Override
    public void deleteWaiting(final Long id) {
        waitingReservationCommandService.delete(id);
    }

    @Override
    @Transactional
    public ReservationResponse promotionWaiting(final Long id, final CreateReservationWithUserIdWebRequest request) {
        final User user = userQueryService.getById(request.userId());
        final Reservation reservation = reservationCommandService.create(request.toServiceRequest());
        waitingReservationCommandService.delete(id);
        return ReservationResponse.from(reservation, user);
    }

    private void promotionWaiting(final Long id, final Long waiting) {
        final Long userId = waitingReservationQueryService.findUserIdById(waiting);
        reservationCommandService.updateUserId(id, userId);
        waitingReservationCommandService.delete(waiting);
    }
}
