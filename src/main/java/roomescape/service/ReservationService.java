package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryDao;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationUpdateDao;
import roomescape.repository.ReservationWaitingQueryDao;
import roomescape.repository.ThemeQueryDao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationQueryDao reservationQueryingDao;
    private final ReservationUpdateDao reservationUpdatingDao;
    private final ReservationTimeQueryDao reservationTimeQueryingDao;
    private final ThemeQueryDao themeQueryingDao;
    private final ReservationWaitingQueryDao reservationWaitingQueryDao;

    public ReservationService(ReservationQueryDao reservationQueryingDao, ReservationUpdateDao reservationUpdatingDao, ReservationTimeQueryDao reservationTimeQueryingDao, ThemeQueryDao themeQueryingDao, ReservationWaitingQueryDao reservationWaitingQueryDao) {
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationUpdatingDao = reservationUpdatingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
        this.reservationWaitingQueryDao = reservationWaitingQueryDao;
    }

    public ReservationResponse read(Long id) {
        Reservation reservationById = getReservation(id);
        return ReservationResponse.from(reservationById);
    }

    public List<ReservationResponse> readAll() {
        List<Reservation> reservations = reservationQueryingDao.findAllReservations();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> readMineByName(String name) {
        List<MyReservationResponse> reservations = reservationQueryingDao.findAllByName(name)
                .stream()
                .map(MyReservationResponse::fromReservation)
                .toList();

        List<MyReservationResponse> waitings = reservationWaitingQueryDao.findAllByName(name)
                .stream()
                .map(MyReservationResponse::fromWaiting)
                .toList();

        return Stream.concat(reservations.stream(), waitings.stream())
                .sorted(Comparator.comparing(MyReservationResponse::getDate)
                        .thenComparing(r -> r.getTime().getStartAt()))
                .toList();
    }

    public ReservationResponse create(ReservationRequest reservationRequest) {
        ReservationTime reservationTimeById = reservationTimeQueryingDao.findReservationTimeById(reservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationRequest.timeId()));
        Theme themeById = themeQueryingDao.findThemeById(reservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationRequest.themeId()));

        validateDuplicatedReservation(new ReservationSlot(reservationRequest.date(), reservationTimeById, themeById));

        Reservation reservation = reservationRequest.toReservation(reservationTimeById, themeById);
        reservation.validatePastDateTime();

        Long generatedId = reservationUpdatingDao.insert(reservation);
        return ReservationResponse.from(reservation.withReservationId(generatedId));
    }

    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existedReservation = getReservation(id);
        existedReservation.validatePastDateTime();

        ReservationTime newTime = reservationTimeQueryingDao.findReservationTimeById(reservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationRequest.timeId()));

        ReservationSlot newSlot = new ReservationSlot(reservationRequest.date(), newTime, existedReservation.getTheme());
        newSlot.validateNoPast();
        validateDuplicatedReservation(newSlot);

        Reservation updatedReservation = existedReservation.withUpdatedDateAndTime(reservationRequest.date(), newTime);

        reservationUpdatingDao.update(id, updatedReservation);
        return ReservationResponse.from(updatedReservation);
    }

    public void delete(Long id) {
        Reservation reservation = getReservation(id);
        reservation.validatePastDateTime();
        reservationUpdatingDao.delete(id);
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약이 존재하지 않습니다."));
    }

    private void validateDuplicatedReservation(ReservationSlot reservationSlot) {
        Optional<Reservation> duplicateReservation = reservationQueryingDao.findReservationBySlot(reservationSlot);
        if (duplicateReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }
    }
}
