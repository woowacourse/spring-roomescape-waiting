package roomescape.reservation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.holiday.service.HolidayService;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.MyReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaitings;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithRank;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.TimeService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeRepository themeRepository;
    private final HolidayService holidayService;
    private final SlotService slotService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            TimeService timeService,
            ThemeRepository themeRepository,
            HolidayService holidayService, SlotService slotService
    ) {
        this.reservationRepository = reservationRepository;
        this.timeService = timeService;
        this.themeRepository = themeRepository;
        this.holidayService = holidayService;
        this.slotService = slotService;
    }

    @Override
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Override
    @Transactional
    public Reservation create(ReservationSaveServiceRequest request) {
        slotService.ensure(request.themeId(), request.timeId());
        slotService.lock(request.themeId(), request.timeId());

        LocalDateTime now = LocalDateTime.now();
        ReservationTime time = timeService.findById(request.timeId());
        time.validateExpired(now);

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (holidayService.isHoliday(time.getStartAt().toLocalDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }

        if (reservationRepository.existsByNameAndTheme_IdAndTime(request.name(), request.themeId(), time)) {
            throw new DuplicateReservationException();
        }
        if (reservationRepository.existsByTheme_IdAndTime(request.themeId(), time)) {
            return reservationRepository.save(new Reservation(request.name(), time, theme, Status.WAITING, now));
        }
        return reservationRepository.save(new Reservation(request.name(), time, theme, Status.RESERVED, now));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Reservation reservationForSlotKey = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        slotService.ensure(reservationForSlotKey.getThemeId(), reservationForSlotKey.getTimeId());
        slotService.lock(reservationForSlotKey.getThemeId(), reservationForSlotKey.getTimeId());
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.validateExpired(LocalDateTime.now());
        if (reservation.isReserved()) {
            ReservationWaitings waitings = new ReservationWaitings(
                    reservationRepository.findAllByTime_IdAndTheme_IdAndStatus(
                            reservation.getTimeId(),
                            reservation.getThemeId(),
                            Status.WAITING));
            waitings.earliest().ifPresent(Reservation::promote);
        }
        reservationRepository.deleteById(id);
    }

    @Override
    public List<ReservationWithWaitingOrderResponse> getAllByName(String name) {
        List<Reservation> mine = reservationRepository.findByName(name);
        return mine.stream()
                .map(reservation -> new MyReservation(reservation, 0))
                .map(ReservationWithWaitingOrderResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void cancelForUser(Long id, String name) {
        Reservation reservationForSlotKey = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        slotService.ensure(reservationForSlotKey.getThemeId(), reservationForSlotKey.getTimeId());
        slotService.lock(reservationForSlotKey.getThemeId(), reservationForSlotKey.getTimeId());

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.validateChangeableBy(name, LocalDateTime.now());
        if (reservation.isReserved()) {
            ReservationWaitings waitings = new ReservationWaitings(
                    reservationRepository.findAllByTime_IdAndTheme_IdAndStatus(
                            reservation.getTimeId(),
                            reservation.getThemeId(),
                            Status.WAITING));
            waitings.earliest().ifPresent(Reservation::promote);
        }
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Reservation update(Long id, Long timeId, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        LocalDateTime now = LocalDateTime.now();
        reservation.validateChangeableBy(name, now);

        ReservationTime newTime = timeService.findById(timeId);
        newTime.validateExpired(now);
        if (holidayService.isHoliday(newTime.getStartAt().toLocalDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }

        if (reservationRepository.existsByNameAndTheme_IdAndTime(name, reservation.getThemeId(), newTime)) {
            throw new DuplicateReservationException();
        }

        if (reservation.isReserved()) {
            ReservationWaitings waitings = new ReservationWaitings(
                    reservationRepository.findAllByTime_IdAndTheme_IdAndStatus(
                            reservation.getTimeId(),
                            reservation.getThemeId(),
                            Status.WAITING));
            waitings.earliest().ifPresent(Reservation::promote);
        }

        reservation.update(newTime);
        return reservation;
    }

    @Override
    public List<ReservationWithRank> findMineWithRank(String name) {
        return reservationRepository.findMineWithRank(name);
    }

    @Override
    public List<ReservationResponse> findMine(String name) {
        reservationRepository.findByName(name);

        return List.of();
    }

    @Override
    public List<ReservationResponse> getWaitings() {
        return reservationRepository.findAllByStatus(Status.WAITING).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public Reservation requestWaiting(ReservationSaveServiceRequest request) {
        LocalDateTime now = LocalDateTime.now();

        ReservationTime time = timeService.findById(request.timeId());
        time.validateExpired(now);

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (holidayService.isHoliday(time.getStartAt().toLocalDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }
        if (reservationRepository.existsByNameAndTheme_IdAndTime(request.name(), request.themeId(), time)) {
            throw new DuplicateReservationException();
        }
        return reservationRepository.save(
                new Reservation(request.name(), time, theme, Status.WAITING, now));
    }

    @Override
    @Transactional
    public void cancelWaiting(Long id, String name) {
        Reservation waiting = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        waiting.validateOwnedBy(name);
        if (waiting.isReserved()) {
            throw new IllegalStateException("대기 상태만 취소할 수 있습니다.");
        }
        reservationRepository.deleteById(id);
    }
}
