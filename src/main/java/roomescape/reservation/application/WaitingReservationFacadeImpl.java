package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.DuplicateException;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.SimpleWaitingReservationResponse;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationViewQueryService;
import roomescape.reservation.application.service.WaitingReservationCommandService;
import roomescape.reservation.application.service.WaitingReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.WaitingReservationResponse;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingReservationFacadeImpl implements WaitingReservationFacade {

    private final ReservationCommandService reservationCommandService;
    private final WaitingReservationCommandService waitingReservationCommandService;
    private final WaitingReservationQueryService waitingReservationQueryService;
    private final ReservationViewQueryService reservationViewQueryService;
    private final UserQueryService userQueryService;

    @Override
    public List<WaitingReservationResponse> getAll() {
        final List<WaitingReservation> waiting = waitingReservationQueryService.getAll();
        final List<Long> userIds = waiting.stream()
                .map(WaitingReservation::getUserId)
                .toList();

        final List<User> users = userQueryService.getAllByIds(userIds);
        return WaitingReservationResponse.from(waiting, users);
    }

    @Override
    public SimpleWaitingReservationResponse create(final CreateReservationWithUserIdWebRequest request) {
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
    public void delete(final Long id) {
        waitingReservationCommandService.delete(id);
    }

    @Override
    @Transactional
    public ReservationResponse promotion(final Long id, final CreateReservationWithUserIdWebRequest request) {
        final User user = userQueryService.getById(request.userId());
        final Reservation reservation = reservationCommandService.create(request.toServiceRequest());
        waitingReservationCommandService.delete(id);
        return ReservationResponse.from(reservation, user);
    }
}
