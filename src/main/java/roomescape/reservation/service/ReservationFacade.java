package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import roomescape.exception.ResourceInUseException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;

@Service
public class ReservationFacade {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;

    private final ConcurrentHashMap<String, ReentrantLock> slotLocks = new ConcurrentHashMap<>();

    public ReservationFacade(ReservationService reservationService, ReservationTimeService reservationTimeService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
    }

    public ReservationCreateResponse createReservation(ReservationRequest request) {
        Lock lock = getSlotLock(LocalDate.parse(request.date()), request.timeId(),
            request.themeId());
        try {
            return reservationService.create(request);
        } finally {
            lock.unlock();
        }
    }

    public void deleteReservationTime(Long id) {
        if (reservationService.existsByTimeId(id)) {
            throw new ResourceInUseException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }

        reservationTimeService.delete(id);
    }

    public ReservationTimeCreateResponse createReservationTime(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        return reservationTimeService.create(reservationTimeCreateRequest);
    }

    public List<ReservationTimeFindAllResponse> findAllReservationTime() {
        return reservationTimeService.findAll();
    }

    public List<ReservationResponse> findAllReservation() {
        return reservationService.findAll();
    }

    public void deleteReservation(Long id) {
        reservationService.delete(id);
    }

    public ReservationResponse findReservationById(Long id) {
        return reservationService.findById(id);
    }

    public void deleteReservedByNameAndReservationId(String name, Long reservationId) {
        ReservationResponse info = reservationService.findById(reservationId);
        Lock lock = getSlotLock(info.date(), info.time().id(), info.theme().id());
        lock.lock();
        try {
            Reservation reservation = reservationService.deleteMyReservation(reservationId, name);
            try {
                reservationService.promoteFirstWaiting(reservation);
            } catch (Exception e) {
                reservationService.restoreReservation(reservation.getId());
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    public void deleteWaitingByNameAndReservationId(String name, Long reservationId) {
        reservationService.deleteWaitingByNameAndReservationId(name, reservationId);
    }

    public void updateMyReservation(UpdateMyReservation updateMyReservation, String name, Long reservationId) {
        Reservation reservation = reservationService.updateMyReservation(updateMyReservation, name,
            reservationId);
        try {
            reservationService.promoteFirstWaiting(reservation);
        } catch (Exception e) {
            reservationService.revertReservationUpdate(
                reservationId,
                reservation.getDate(),
                reservation.getTime().getId(),
                name
            );
            throw e;
        }
    }

    public List<MyReservationResponse> findReservationsByName(String name) {
        return reservationService.findAllByName(name);
    }

    private Lock getSlotLock(LocalDate date, Long timeId, Long themeId) {
        String key = date + ":" + timeId + ":" + themeId;
        return slotLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }
}
