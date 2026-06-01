package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.holiday.service.HolidayService;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.MyReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaitings;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.TimeService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeRepository themeRepository;
    private final HolidayService holidayService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            TimeService timeService,
            ThemeRepository themeRepository,
            HolidayService holidayService
    ) {
        this.reservationRepository = reservationRepository;
        this.timeService = timeService;
        this.themeRepository = themeRepository;
        this.holidayService = holidayService;
    }

    @Override
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Override
    @Transactional
    public Reservation create(ReservationSaveServiceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ReservationTime time = timeService.findById(request.timeId());
        time.validateExpired(now);

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (holidayService.isHoliday(time.getDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }

        if (reservationRepository.isDuplicatedWithName(request.name(), request.themeId(), time)) {
            throw new DuplicateReservationException();
        }
        if (reservationRepository.isDuplicated(request.themeId(), time)) {
            return reservationRepository.save(new Reservation(request.name(), time, theme, Status.WAITING, now));
        }
        return reservationRepository.save(new Reservation(request.name(), time, theme, Status.RESERVED, now));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        boolean deleted = reservationRepository.deleteById(id);
        if (!deleted) {
            throw new ReservationNotFoundException(id);
        }
    }

    @Override
    public List<ReservationWithWaitingOrderResponse> getAllByName(String name) {
        List<Reservation> myReservations = reservationRepository.findByName(name);

        return myReservations.stream()
                .map(reservation -> MyReservation.of(
                        reservation,
                        new ReservationWaitings(
                                reservationRepository.findAllWaitingBy(
                                        reservation.getTime().getId(),
                                        reservation.getTheme().getId()))))
                .map(ReservationWithWaitingOrderResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void cancelForUser(Long id, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.validateCancelBy(name, LocalDateTime.now());

        if (reservation.isReserved()) {
            ReservationWaitings waitings = new ReservationWaitings(
                    reservationRepository.findAllWaitingBy(
                            reservation.getTime().getId(),
                            reservation.getTheme().getId()));
            waitings.earliest()
                    .map(Reservation::promote)
                    .ifPresent(reservationRepository::update);
        }
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Reservation update(Long id, Long timeId, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        LocalDateTime now = LocalDateTime.now();
        reservation.validateUpdateBy(name, now);

        ReservationTime newTime = timeService.findById(timeId);
        newTime.validateExpired(now);

        if (holidayService.isHoliday(newTime.getDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }

        if (reservationRepository.isDuplicated(reservation.getTheme().getId(), newTime)) {
            throw new DuplicateReservationException();
        }

        Reservation updatedReservation = reservation.withTime(newTime).withCreatedAt(now);
        return reservationRepository.update(updatedReservation);
    }
}
