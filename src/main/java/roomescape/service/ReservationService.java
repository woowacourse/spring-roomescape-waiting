package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationAndWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.reservation.UserReservations;
import roomescape.domain.reservation.ReservationLine;
import roomescape.domain.reservation.WaitingWithNumber;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotOwnerException;
import roomescape.exception.NotFoundException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;

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

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, long timeId, long themeId,
                                       LocalDateTime requestTime) {
        Reservation reservation = createReservation(name, date, timeId, themeId, requestTime);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void removeReservation(long id, String requestName, LocalDateTime requestTime) {
        reservationRepository.findById(id)
                .ifPresent(reservation ->
                        deleteReservationAndPromoteWaiting(reservation, requestName, requestTime));
    }

    @Transactional
    public void updateReservation(long id, String requestName, LocalDate date, long timeId,
                                  LocalDateTime requestTime) {

        Reservation nowReservation = getValidatedReservation(id, requestName);
        ReservationSlot updateSlot = findOrCreateReservationSlot(date, timeId, nowReservation.getTheme().getId());

        lockReservationSlots(nowReservation.getSlot(), updateSlot);
        ReservationLine nowReservationLine = createReservationLine(nowReservation.getSlot());

        if (nowReservationLine.validateAndCheckSameSlot(nowReservation, updateSlot, requestTime)) {
            return;
        }

        moveReservation(nowReservation, updateSlot, requestTime, nowReservationLine);
    }

    private Reservation createReservation(String name, LocalDate date, long timeId, long themeId,
                                          LocalDateTime requestTime) {
        ReservationSlot reservationSlot = findOrCreateReservationSlot(date, timeId, themeId);
        ReservationSlot lockedSlot = getLockedReservationSlot(reservationSlot);
        ReservationLine reservationLine = createReservationLine(lockedSlot);

        return reservationLine.add(name, requestTime);
    }

    private void deleteReservationAndPromoteWaiting(Reservation reservation, String requestName,
                                                    LocalDateTime requestTime) {
        validateReservationOwner(reservation, requestName);

        ReservationSlot lockedSlot = getLockedReservationSlot(reservation.getSlot());
        ReservationLine reservationLine = createReservationLine(lockedSlot);
        Optional<Reservation> promotedReservation = reservationLine.findPromotedReservationAfterCancel(
                reservation,
                requestTime
        );

        deleteReservation(reservation);
        promotedReservation.ifPresent(reservationRepository::update);
    }

    private void deleteReservation(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    private void moveReservation(Reservation nowReservation, ReservationSlot updateSlot, LocalDateTime requestTime,
                                 ReservationLine nowReservationLine) {
        ReservationLine updateReservationLine = createReservationLine(updateSlot);
        Reservation updateReservation = updateReservationLine.move(nowReservation, requestTime);
        Optional<Reservation> promotedReservation = nowReservationLine.findNextToPromote(nowReservation);

        reservationRepository.update(updateReservation);
        promotedReservation.ifPresent(reservationRepository::update);
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

        Map<Long, List<Reservation>> reservationsBySlotId = findReservationsBySlotId(waitingsByName);

        return waitingsByName.stream()
                .map(waiting -> createWaitingWithNumber(waiting, reservationsBySlotId))
                .toList();
    }

    private Map<Long, List<Reservation>> findReservationsBySlotId(List<Reservation> waitingsByName) {
        List<Long> slotIds = waitingsByName.stream()
                .map(waiting -> waiting.getSlot().getId())
                .distinct()
                .toList();

        if (slotIds.isEmpty()) {
            return Map.of();
        }

        return reservationRepository.findBySlotIds(slotIds).stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getSlot().getId()));
    }

    private ReservationLine createReservationLine(ReservationSlot slot) {
        return new ReservationLine(slot, reservationRepository.findBySlotId(slot.getId()));
    }

    private Reservation getValidatedReservation(long id, String requestName) {
        Reservation reservation = getReservationById(id);
        validateReservationOwner(reservation, requestName);
        return reservation;
    }

    private ReservationSlot findOrCreateReservationSlot(LocalDate date, long timeId, long themeId) {
        return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId,
                themeId).orElseGet(() -> createOrFindSlot(date, timeId, themeId));
    }

    private ReservationSlot createOrFindSlot(LocalDate date, long timeId, long themeId) {
        try {
            return createReservationSlot(date, timeId, themeId);
        } catch (DuplicateException e) {
            return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                    .orElseThrow(() -> new NotFoundException("해당하는 예약 슬롯을 찾을 수 없습니다."));
        }
    }

    private ReservationSlot createReservationSlot(LocalDate date, long timeId, long themeId) {
        TimeSlot timeSlot = getTimeSlot(timeId);
        Theme theme = getTheme(themeId);
        return reservationSlotRepository.save(new ReservationSlot(date, timeSlot, theme));
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
                                                      Map<Long, List<Reservation>> reservationsBySlotId) {

        List<Reservation> sameSlotReservations = reservationsBySlotId.getOrDefault(waiting.getSlot().getId(), List.of());
        ReservationLine reservationLine = new ReservationLine(waiting.getSlot(), sameSlotReservations);

        int waitingIndex = reservationLine.findWaitingIndex(waiting)
                .orElseThrow(() -> new IllegalArgumentException("예약 대기 순번을 계산할 수 없습니다."));
        return new WaitingWithNumber(waiting, waitingIndex);
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
