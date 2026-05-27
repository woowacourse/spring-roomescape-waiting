//package roomescape.service;
//
//import java.time.LocalDateTime;
//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.stereotype.Service;
//
//import roomescape.domain.Reservation;
//import roomescape.domain.ReservationSlot;
//import roomescape.dto.ReservationResponse;
//import roomescape.dto.WaitingRequest;
//import roomescape.dto.WaitingResponse;
//import roomescape.exception.CustomException;
//import roomescape.exception.ErrorCode;
//import roomescape.repository.ReservationSlotDao;
//import roomescape.repository.ReservationDao;
//
//@Service
//public class WaitingService {
//    private final ReservationDao reservationDao;
//    private final ReservationSlotDao reservationSlotDao;
//
//    public WaitingService(ReservationDao reservationDao, ReservationSlotDao reservationSlotDao) {
//        this.reservationDao = reservationDao;
//        this.reservationSlotDao = reservationSlotDao;
//    }
//
//    public WaitingResponse save(LocalDateTime now, WaitingRequest request) {
//        ReservationSlot reservationSlot = reservationSlotDao.findById(request.reservationId());
//        LocalDateTime time = LocalDateTime.of(reservationSlot.getDate(), reservationSlot.getTime().getStartAt());
//        validateDateAndTimeNotPast(now,time);
//        try{
//            long waitingId = reservationDao.save(request.name(), request.reservationId());
//            int order =  reservationDao.findOrderByReservationId(waitingId, request.reservationId());
//            Reservation reservation = new Reservation(waitingId, request.name(), request.reservationId());
//            return WaitingResponse.from(reservation, order);
//        } catch (DataIntegrityViolationException e) {
//            throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
//        }
//
//    }
//
//    public void delete(LocalDateTime now, Long id) {
//        Reservation reservation = reservationDao.findById(id);
//        ReservationSlot reservationSlot = reservationSlotDao.findById(reservation.getReservationId());
//        LocalDateTime time = LocalDateTime.of(reservationSlot.getDate(), reservationSlot.getTime().getStartAt());
//        validateDateAndTimeNotPast(now, time);
//
//        reservationDao.delete(id);
//    }
//
//    public ReservationResponse findById(long id) {
//        Reservation reservation = reservationDao.findById(id);
//        ReservationSlot reservationSlot = reservationSlotDao.findById(reservation.getReservationId());
//        return ReservationResponse.from(reservationSlot, WaitingResponse.from(reservation, reservationDao.findOrderByReservationId(id, reservationSlot.getId())));
//    }
//
//    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
//        if (now.isAfter(reservationTime)) {
//            throw new CustomException(ErrorCode.PAST_DATE_RESERVATION);
//        }
//    }
//}
