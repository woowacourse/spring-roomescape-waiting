package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservation.dto.ReservationIdResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
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
        ReservationSlot slot = new ReservationSlot(request.date(), time, theme);

        if (reservationRepository.isBooked(slot)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }
        try {
            Reservation saved = reservationRepository.save(reservationFactory.create(request.name(), slot));
            return ReservationResponse.from(saved);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        List<Reservation> reservations = (name != null)
                ? reservationRepository.findByName(name)
                : reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getById(id);
        if (!reservation.isCancelable(clock)) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_CANCEL);
        }
        reservationRepository.deleteById(id);

        reservationWaitingService.promoteWaiting(reservation.getSlot());
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        if (!reservation.isCancelable(clock)) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_UPDATE);
        }
        ReservationSlot slot = reservation.getSlot();
        ReservationTime time = reservationTimeService.getById(request.timeId());
        ReservationSlot newSlot = new ReservationSlot(request.date(), time, slot.theme());

        if (reservationRepository.isBookedByOther(newSlot, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        Reservation validReservation = reservation.reschedule(request.date(), time, clock);
        reservationRepository.update(id, validReservation.getSlot());

        reservationWaitingService.promoteWaiting(slot);
        return ReservationResponse.from(getById(id));
    }

    @NonNull
    private Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public ReservationIdResponse getReservationId(LocalDate date, Long themeId, Long timeId) {
        return reservationRepository.findIdBySlot(date, themeId, timeId);
    }
}
