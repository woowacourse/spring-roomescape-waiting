package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationAndWaiting;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.UserReservations;
import roomescape.domain.ReservationLine;
import roomescape.domain.WaitingNumber;
import roomescape.domain.WaitingWithNumber;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotOwnerException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationSlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

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

    public Reservation getReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    public List<ReservationAndWaiting> findReservationAndWaitingByName(String name) {
        List<Reservation> reservationsByName = reservationRepository.findByName(name);
        List<Reservation> reservations = findReservedReservations(reservationsByName);
        List<WaitingWithNumber> waitings = createWaitingsWithNumber(reservationsByName);

        UserReservations userReservations = new UserReservations(name, reservations, waitings);
        return userReservations.getReservationAndWaitings();
    }

    private List<Reservation> findReservedReservations(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Reservation::isReserved)
                .toList();
    }

    private List<WaitingWithNumber> createWaitingsWithNumber(List<Reservation> reservationsByName) {
        List<Reservation> waitingsByName = reservationsByName.stream()
                .filter(Reservation::isWaiting)
                .toList();

        Map<Long, List<Reservation>> waitingsBySlotId = findWaitingsBySlotId(waitingsByName);

        return waitingsByName.stream()
                .map(waiting -> createWaitingWithNumber(waiting, waitingsBySlotId))
                .toList();
    }

    private Map<Long, List<Reservation>> findWaitingsBySlotId(List<Reservation> waitingsByName) {
        List<Long> slotIds = waitingsByName.stream()
                .map(waiting -> waiting.getSlot().getId())
                .distinct()
                .toList();

        return reservationRepository.findWaitingsBySlotIds(slotIds).stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getSlot().getId()));
    }

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, long timeId, long themeId) {
        LocalDateTime requestTime = LocalDateTime.now();
        Reservation reservation = createReservation(name, date, timeId, themeId, requestTime);
        return reservationRepository.save(reservation);
    }

    private Reservation createReservation(String name, LocalDate date, long timeId, long themeId,
                                          LocalDateTime requestTime) {
        ReservationSlot reservationSlot = findOrCreateReservationSlot(date, timeId, themeId);
        ReservationSlot lockedSlot = getLockedReservationSlot(reservationSlot);
        ReservationLine reservationLine = new ReservationLine(lockedSlot, reservationRepository.findBySlotId(lockedSlot.getId()));

        return reservationLine.add(name, requestTime);
    }

    private ReservationSlot createReservationSlot(LocalDate date, long timeId, long themeId) {
        TimeSlot timeSlot = getTimeSlot(timeId);
        Theme theme = getTheme(themeId);
        return reservationSlotRepository.save(new ReservationSlot(date, timeSlot, theme));
    }

    @Transactional
    public void removeReservation(long id, String requestName) {
        LocalDateTime requestTime = LocalDateTime.now();
        reservationRepository.findById(id)
                .ifPresent(reservation ->
                        deleteReservationAndPromoteWaiting(reservation, requestName, requestTime));
    }

    private void deleteReservationAndPromoteWaiting(Reservation reservation, String requestName,
                                                    LocalDateTime requestTime) {
        validateReservationOwner(reservation, requestName);
        reservation.cancel(requestTime);

        ReservationSlot lockedSlot = getLockedReservationSlot(reservation.getSlot());

        deleteReservation(reservation);
        if (reservation.isReserved()) {
            promoteFirstWaiting(lockedSlot);
        }
    }

    private void deleteReservation(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    private void promoteFirstWaiting(ReservationSlot slot) {
        ReservationLine waitings = new ReservationLine(slot, reservationRepository.findWaitingsBySlotId(slot.getId()));
        waitings.promoteFirstWaiting()
                .ifPresent(reservationRepository::update);
    }

    @Transactional
    public void updateReservation(long id, String requestName, LocalDate date, long timeId) {
        LocalDateTime requestTime = LocalDateTime.now();
        Reservation nowReservation = getReservationById(id);
        validateReservationOwner(nowReservation, requestName);

        long themeId = nowReservation.getTheme().getId();
        ReservationSlot updateSlot = findOrCreateReservationSlot(date, timeId, themeId);
        Reservation updateReservation = nowReservation.updateSlot(updateSlot, requestTime);

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

    private ReservationSlot findOrCreateReservationSlot(LocalDate date, long timeId, long themeId) {
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
            getLockedReservationSlot(nowSlot);
            getLockedReservationSlot(updateSlot);
            return;
        }

        getLockedReservationSlot(updateSlot);
        getLockedReservationSlot(nowSlot);
    }

    private ReservationSlot getLockedReservationSlot(ReservationSlot reservationSlot) {
        return reservationSlotRepository.findByIdWithLock(reservationSlot.getId())
                .orElseThrow(() -> new NotFoundException("해당 예약 슬롯을 찾을 수 없습니다."));
    }

    private WaitingWithNumber createWaitingWithNumber(Reservation waiting,
                                                      Map<Long, List<Reservation>> waitingsBySlotId) {

        List<Reservation> sameSlotWaitings = waitingsBySlotId.getOrDefault(waiting.getSlot().getId(), List.of());
        ReservationLine reservationLine = new ReservationLine(waiting.getSlot(), sameSlotWaitings);
        return new WaitingWithNumber(waiting, WaitingNumber.fromIndex(reservationLine.findWaitingIndex(waiting)));
    }

    private TimeSlot getTimeSlot(long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 시간대를 찾을 수 없습니다."));
    }

    private Theme getTheme(long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
    }
}
