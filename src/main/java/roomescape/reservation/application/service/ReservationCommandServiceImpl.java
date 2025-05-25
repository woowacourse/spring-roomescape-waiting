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
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.time.application.service.ReservationTimeQueryService;
import roomescape.time.domain.ReservationTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandServiceImpl implements ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final TimeProvider timeProvider;

    @Override
    public Reservation create(final CreateReservationServiceRequest request) {
        if (isExistsByParams(request.date(), request.timeId(), request.themeId())) {
            throw new DuplicateException(
                    DomainTerm.RESERVATION,
                    request.date(),
                    request.timeId(),
                    request.themeId());
        }
        final ReservationTime reservationTime = reservationTimeQueryService.get(request.timeId());

        final Theme theme = themeQueryService.get(request.themeId());

        final Reservation reservation = request.toDomain(reservationTime, theme);
        reservation.validatePast(timeProvider.now());
        return reservationRepository.save(reservation);
    }

    @Override
    public void delete(final Long id) {
        if (reservationRepository.existsByParams(id)) {
            reservationRepository.deleteById(id);
            return;
        }

        throw new NotFoundException(DomainTerm.RESERVATION, id);
    }

    @Override
    public WaitingReservation createWaitingReservation(final CreateReservationServiceRequest request) {
        if (!isExistsByParams(request.date(), request.timeId(), request.themeId())) {
            throw new NotFoundException(DomainTerm.RESERVATION, //Todo 예외와 중복 생성 검증 필요, 예약 없을 시 처리도 필요
                    request.date(),
                    DomainTerm.THEME_ID,
                    DomainTerm.RESERVATION_TIME_ID);
        }
        if (waitingReservationRepository.existsByParams(
                request.date(), request.timeId(), request.themeId())) {
            throw new DuplicateException(DomainTerm.RESERVATION_WAITING,
                    request.date(),
                    DomainTerm.THEME_ID,
                    DomainTerm.RESERVATION_TIME_ID);
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

    @Override
    public void deleteWaiting(final Long id) {
        final WaitingReservation waitingReservation = waitingReservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DomainTerm.RESERVATION_WAITING, id));

        waitingReservationRepository.decrementWaitingOrderAfter(
                waitingReservation.getDate(),
                waitingReservation.getTime(),
                waitingReservation.getTheme(),
                waitingReservation.getWaitingOrder());
        waitingReservationRepository.deleteById(id);
    }

    private boolean isExistsByParams(final ReservationDate date,
                                     final Long timeId,
                                     final Long themeId) {
        return reservationQueryService.existsByParams(
                date,
                timeId,
                themeId
        );
    }
}
