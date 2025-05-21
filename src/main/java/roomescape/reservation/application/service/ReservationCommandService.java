package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.time.TimeProvider;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final TimeProvider timeProvider;

    public Reservation create(final CreateReservationRequest request) {
        final ReservationTime reservationTime = reservationTimeQueryService.get(
                request.timeId());

        final Theme theme = themeQueryService.get(
                request.themeId());

        if (reservationQueryService.existsByParams(
                request.date(),
                reservationTime.getStartAt(),
                request.themeId())
        ) {
            throw new DuplicateException(
                    DomainTerm.RESERVATION,
                    request.date(),
                    reservationTime.getStartAt(),
                    request.themeId());
        }

        final Reservation reservation = request.toDomain(theme, reservationTime.getStartAt());
        reservation.validatePast(timeProvider.now());

        return reservationRepository.save(reservation);
    }

    public void delete(final ReservationId id) {
        if (reservationRepository.existsByParams(id)) {
            reservationRepository.deleteById(id);
            return;
        }

        throw new NotFoundException(DomainTerm.RESERVATION, id);
    }

    public void delete(final Reservation target) {
        reservationRepository.delete(target);
    }
}
