package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationAndWaiting;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.UserReservations;
import roomescape.domain.WaitingLine;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotOwnerException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationSlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.domain.WaitingWithNumber;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            TimeSlotRepository timeSlotRepository,
            ThemeRepository themeRepository,
            ReservationSlotRepository reservationSlotRepository
    ) {

        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.reservationSlotRepository = reservationSlotRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    public List<ReservationAndWaiting> findReservationAndWaitingByName(String name) {
        List<Reservation> reservationsByName = reservationRepository.findByName(name);
        List<Reservation> reservations = reservationsByName.stream()
                .filter(Reservation::isReserved)
                .toList();

        List<WaitingWithNumber> waitings = reservationsByName.stream()
                .filter(Reservation::isWaiting)
                .map(this::createWaitingWithNumber)
                .toList();

        UserReservations userReservations = new UserReservations(name, reservations, waitings);
        return userReservations.getReservationAndWaitings();
    }

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Reservation reservation = createReservation(name, date, timeId, themeId);
        return reservationRepository.save(reservation);
    }

    private Reservation createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationSlot reservationSlot = findOrCreateReservationSlot(date, timeId, themeId);
        ReservationSlot lockedSlot = findLockedReservationSlot(reservationSlot);
        WaitingLine waitingLine = new WaitingLine(lockedSlot, reservationRepository.findBySlotId(lockedSlot.getId()));

        return waitingLine.add(name, LocalDateTime.now());
    }

    private ReservationSlot createReservationSlot(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlot(timeId);
        Theme theme = findTheme(themeId);
        return reservationSlotRepository.save(new ReservationSlot(date, timeSlot, theme));
    }

    @Transactional
    public void removeReservation(long id, String requestName) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> deleteReservationAndPromoteWaiting(reservation, requestName));
    }

    private void deleteReservationAndPromoteWaiting(Reservation reservation, String requestName) {
        validateReservationOwner(reservation, requestName);
        reservation.validateCancelable(LocalDateTime.now());

        ReservationSlot lockedSlot = findLockedReservationSlot(reservation.getSlot());

        deleteReservation(reservation);
        if (reservation.isReserved()) {
            promoteFirstWaiting(lockedSlot);
        }
    }

    private void deleteReservation(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    private void promoteFirstWaiting(ReservationSlot slot) {
        WaitingLine waitings = new WaitingLine(slot, reservationRepository.findWaitingsBySlotId(slot.getId()));
        waitings.promoteFirstWaiting()
                .ifPresent(reservationRepository::update);
    }

    @Transactional
    public void updateReservation(long id, String requestName, LocalDate date, Long timeId) {
        Reservation nowReservation = findReservationById(id);
        validateReservationOwner(nowReservation, requestName);

        Long themeId = nowReservation.getTheme().getId();
        ReservationSlot updateSlot = findOrCreateReservationSlot(date, timeId, themeId);
        Reservation updateReservation = nowReservation.updateSlot(updateSlot, LocalDateTime.now());

        if (nowReservation.hasSameDateAndTime(updateReservation)) {
            return;
        }

        lockReservationSlots(nowReservation.getSlot(), updateSlot);
        validateReservedSlot(updateSlot);
        reservationRepository.update(updateReservation);

        if (nowReservation.isReserved()) {
            promoteFirstWaiting(nowReservation.getSlot());
        }
    }

    private ReservationSlot findOrCreateReservationSlot(LocalDate date, Long timeId, Long themeId) {
        return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId,
                themeId).orElseGet(() -> createReservationSlot(date, timeId, themeId));
    }

    private void validateReservedSlot(ReservationSlot slot) {
        if (reservationRepository.existsReservedBySlotId(slot.getId())) {
            throw new DuplicateException("이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private void validateReservationOwner(Reservation reservation, String requestName) {
        if (!reservation.isOwner(requestName)) {
            throw new NotOwnerException();
        }
    }

    private void lockReservationSlots(ReservationSlot nowSlot, ReservationSlot updateSlot) {
        if (nowSlot.getId() < updateSlot.getId()) {
            findLockedReservationSlot(nowSlot);
            findLockedReservationSlot(updateSlot);
            return;
        }

        findLockedReservationSlot(updateSlot);
        findLockedReservationSlot(nowSlot);
    }

    private ReservationSlot findLockedReservationSlot(ReservationSlot reservationSlot) {
        return reservationSlotRepository.findByIdWithLock(reservationSlot.getId())
                .orElseThrow(() -> new NotFoundException("해당 예약 슬롯을 찾을 수 없습니다."));
    }

    private WaitingWithNumber createWaitingWithNumber(Reservation waiting) {
        WaitingLine waitingLine = new WaitingLine(waiting.getSlot(),
                reservationRepository.findWaitingsBySlotId(waiting.getSlot().getId()));
        return new WaitingWithNumber(waiting, waitingLine.findWaitingNumber(waiting));
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 시간대를 찾을 수 없습니다."));
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
    }
}
