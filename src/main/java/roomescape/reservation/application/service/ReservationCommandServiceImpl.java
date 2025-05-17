package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.time.TimeProvider;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final TimeProvider timeProvider;

    @Override
    public Reservation create(final CreateReservationServiceRequest request) {
        if (reservationQueryService.existsByParams(
                request.date(),
                request.timeId(),
                request.themeId())
        ) {
            throw new DuplicateException(
                    DomainTerm.RESERVATION,
                    request.date(),
                    request.timeId(),
                    request.themeId());
        }

        final ReservationTime reservationTime = reservationTimeQueryService.get(
                request.timeId());

        final Theme theme = themeQueryService.get(
                request.themeId());

        final Reservation reservation = request.toDomain(reservationTime, theme);
        reservation.validatePast(timeProvider.now());

        return reservationRepository.save(reservation);
    }

    @Override
    public void delete(final ReservationId id) {
        if (reservationRepository.existsByParams(id)) {
            reservationRepository.deleteById(id);
            return;
        }

        throw new NotFoundException(DomainTerm.RESERVATION, id);
    }
}
