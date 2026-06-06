package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationAndWaiting;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
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
        validateDuplicatedReservation(name, reservation.getSlot());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void removeReservation(long id, String requestName) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> deleteReservation(reservation, requestName));
    }

    private ReservationSlot findOrCreateReservationSlot(LocalDate date, Long timeId, Long themeId) {
        return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId,
                themeId).orElseGet(() -> createReservationSlot(date, timeId, themeId));
    }

    private ReservationSlot createReservationSlot(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlot(timeId);
        Theme theme = findTheme(themeId);
        return reservationSlotRepository.save(new ReservationSlot(date, timeSlot, theme));
    }

    private void deleteReservation(Reservation reservation, String requestName) {
        validateReservationOwner(reservation, requestName);
        reservation.validateCancelable(LocalDateTime.now());
        reservationRepository.deleteById(reservation.getId());
    }

    @Transactional
    public void updateReservation(long id, String requestName, LocalDate date, Long timeId) {
        Reservation nowReservation = findReservationById(id);
        validateReservationOwner(nowReservation, requestName);

        Long themeId = nowReservation.getTheme().getId();
        ReservationSlot updateSlot = findOrCreateReservationSlot(date, timeId, themeId);
        Reservation updatedReservation = nowReservation.updateSlot(updateSlot, LocalDateTime.now());

        if (!nowReservation.hasSameDateAndTime(updatedReservation)) {
            validateReservedSlot(date, timeId, themeId);
            reservationRepository.update(updatedReservation);
        }
    }

    private Reservation createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationSlot reservationSlot = findOrCreateReservationSlot(date, timeId, themeId);
        boolean isReserved = reservationRepository.existsReservedBySlot(date, timeId, themeId);
        ReservationStatus status = ReservationStatus.RESERVED;
        if (isReserved) {
            status = ReservationStatus.WAITING;
        }
        return new Reservation(name, reservationSlot, LocalDateTime.now(), status);
    }

    private void validateDuplicatedReservation(String name, ReservationSlot reservationSlot) {
        if (reservationRepository.existsByNameAndSlotId(name, reservationSlot.getId())) {
            throw new DuplicateException("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private void validateReservedSlot(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsReservedBySlot(date, timeId, themeId)) {
            throw new DuplicateException("이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private void validateReservationOwner(Reservation reservation, String requestName) {
        if (!reservation.isOwner(requestName)) {
            throw new NotOwnerException();
        }
    }

    private WaitingWithNumber createWaitingWithNumber(Reservation waiting) {
        WaitingLine waitingLine = new WaitingLine(reservationRepository.findWaitingsBySlotId(waiting.getSlot().getId()));
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
