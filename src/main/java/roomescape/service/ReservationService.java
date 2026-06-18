package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationQueryDao;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationUpdateDao;
import roomescape.repository.ReservationWaitingQueryDao;
import roomescape.repository.ReservationWaitingUpdateDao;
import roomescape.repository.ThemeQueryDao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservationQueryDao reservationQueryDao;
    private final ReservationUpdateDao reservationUpdateDao;
    private final ReservationTimeQueryDao reservationTimeQueryDao;
    private final ThemeQueryDao themeQueryDao;
    private final ReservationWaitingQueryDao reservationWaitingQueryDao;
    private final ReservationWaitingUpdateDao reservationWaitingUpdateDao;
    private final OrderRepository orderRepository;

    public ReservationService(ReservationQueryDao reservationQueryDao, ReservationUpdateDao reservationUpdateDao,
                              ReservationTimeQueryDao reservationTimeQueryDao, ThemeQueryDao themeQueryDao,
                              ReservationWaitingQueryDao reservationWaitingQueryDao,
                              ReservationWaitingUpdateDao reservationWaitingUpdateDao,
                              OrderRepository orderRepository) {
        this.reservationQueryDao = reservationQueryDao;
        this.reservationUpdateDao = reservationUpdateDao;
        this.reservationTimeQueryDao = reservationTimeQueryDao;
        this.themeQueryDao = themeQueryDao;
        this.reservationWaitingQueryDao = reservationWaitingQueryDao;
        this.reservationWaitingUpdateDao = reservationWaitingUpdateDao;
        this.orderRepository = orderRepository;
    }

    public ReservationResponse read(Long id) {
        Reservation reservationById = getReservation(id);
        return ReservationResponse.from(reservationById);
    }

    public List<ReservationResponse> readAll() {
        List<Reservation> reservations = reservationQueryDao.findAllReservations();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> readMineByName(String name) {
        List<MyReservationResponse> reservations = reservationQueryDao.findAllByName(name)
                .stream()
                .map(reservation -> orderRepository
                        .findByReservation(name, reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                        .map(order -> MyReservationResponse.fromReservation(reservation, order))
                        .orElseGet(() -> MyReservationResponse.fromReservationWithoutOrder(reservation)))
                .toList();

        List<MyReservationResponse> waitings = reservationWaitingQueryDao.findAllByName(name)
                .stream()
                .map(waitingSequence -> MyReservationResponse.fromWaiting(waitingSequence.reservationWaiting(), waitingSequence.sequence()))
                .toList();

        return Stream.concat(reservations.stream(), waitings.stream())
                .sorted(Comparator.comparing(MyReservationResponse::getDate)
                        .thenComparing(r -> r.getTime().getStartAt()))
                .toList();
    }

    public ReservationResponse create(ReservationRequest reservationRequest) {
        ReservationTime reservationTimeById = reservationTimeQueryDao.findReservationTimeById(reservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationRequest.timeId()));
        Theme themeById = themeQueryDao.findThemeById(reservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationRequest.themeId()));

        validateDuplicatedReservation(new ReservationSlot(reservationRequest.date(), reservationTimeById, themeById));

        Reservation reservation = reservationRequest.toReservation(reservationTimeById, themeById);
        reservation.validatePastDateTime();

        try {
            Long generatedId = reservationUpdateDao.insert(reservation);
            return ReservationResponse.from(reservation.withReservationId(generatedId));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existedReservation = getReservation(id);
        existedReservation.validatePastDateTime();

        ReservationTime newTime = reservationTimeQueryDao.findReservationTimeById(reservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationRequest.timeId()));

        Reservation updatedReservation = existedReservation.withUpdatedDateAndTime(reservationRequest.date(), newTime);
        updatedReservation.validatePastDateTime();
        validateDuplicatedReservation(updatedReservation.getSlot());

        try {
            reservationUpdateDao.update(id, updatedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationAlreadyExistException();
        }
        return ReservationResponse.from(updatedReservation);
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = getReservation(id);
        reservation.validatePastDateTime();
        reservationUpdateDao.delete(id);

        reservationWaitingQueryDao.findFirstWaitingBySlot(reservation.getSlot())
                .ifPresent(waiting -> {
                    reservationUpdateDao.insert(waiting.promoteToReservation());
                    reservationWaitingUpdateDao.delete(waiting.getId());
                        });
    }

    private Reservation getReservation(Long id) {
        return reservationQueryDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약이 존재하지 않습니다."));
    }

    private void validateDuplicatedReservation(ReservationSlot reservationSlot) {
        Optional<Reservation> duplicateReservation = reservationQueryDao.findReservationBySlot(reservationSlot);
        if (duplicateReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }
    }
}
