package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingReservationCommandService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;

    public WaitingReservation create(final CreateReservationServiceRequest request) {
        if (!reservationQueryService.existsByParams(request.date(), request.timeId(), request.themeId())) {
            throw new NotFoundException(DomainTerm.RESERVATION, request.date(), DomainTerm.RESERVATION_TIME, DomainTerm.THEME);
        }

        final ReservationTime reservationTime = reservationTimeQueryService.get(request.timeId());
        final Theme theme = themeQueryService.get(request.themeId());
        final int nextOrder = waitingReservationRepository
                .findMaxWaitingByParams(request.date(), reservationTime, theme) + 1;

        final WaitingReservation waitingReservation = WaitingReservation.withoutId(
                request.userId(),
                nextOrder,
                request.date(),
                reservationTime,
                theme
        );

        return waitingReservationRepository.save(waitingReservation);
    }

    public void delete(final Long id) {
        final WaitingReservation waitingReservation = waitingReservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DomainTerm.RESERVATION_WAITING, id));

        waitingReservationRepository.decrementWaitingOrderAfter(
                waitingReservation.getDate(),
                waitingReservation.getTime(),
                waitingReservation.getTheme(),
                waitingReservation.getWaitingOrder());
        waitingReservationRepository.deleteById(id);
    }
}
