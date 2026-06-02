package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservation.dto.ReservationIdResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.service.ReservationWaitingService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationFactory reservationFactory;
    private final Clock clock;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            ReservationFactory reservationFactory,
            Clock clock,
            ReservationWaitingService reservationWaitingService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationFactory = reservationFactory;
        this.clock = clock;
        this.reservationWaitingService = reservationWaitingService;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        if (reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        Reservation saved = reservationRepository.save(
                reservationFactory.create(request.name(), request.date(), time, theme));
        return ReservationResponse.from(saved);
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        return reservationRepository.findByName(name).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_CANCEL);
        }
        reservationRepository.deleteById(id);

        Optional<ReservationWaiting> promoteWaiting = reservationWaitingService.promoteWaiting(reservation.getDate(),
                reservation.getTime().getId(), reservation.getTheme().getId());
        promoteWaiting.ifPresent(w -> reservationRepository.save(
                reservationFactory.create(w.getName(), w.getDate(), w.getTime(), w.getTheme())));
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        if (reservation.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_UPDATE);
        }
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdExcludingId(request.date(), request.timeId(),
                reservation.getTheme().getId(), id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        reservation.reschedule(request.date(), reservationTimeService.getById(request.timeId()), clock);
        reservationRepository.update(id, request.date(), request.timeId());
        return ReservationResponse.from(getById(id));
    }

    @NonNull
    private Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public ReservationIdResponse getReservationId(LocalDate date, Long themeId, Long timeId) {
        return reservationRepository.findReservationId(date, themeId, timeId);
    }
}
