package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.Time;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationDao;
import roomescape.dto.ReservationRequest;
import roomescape.repository.TimeDao;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;

    public ReservationService(ReservationDao reservationDao, TimeDao timeDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
    }


    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        Time reservationTime = timeDao.findById(request.timeId());
        LocalDateTime time = LocalDateTime.of(request.date(), reservationTime.getStartAt());
        validateDateAndTimeNotPast(now,time);

        try{
            Long reservationId = reservationDao.save(request.date(), request.timeId(), request.themeId());
            Reservation reservation = reservationDao.findById(reservationId);
            return ReservationResponse.from(reservation);
        } catch (DuplicateKeyException e){
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public ReservationResponse findById(long id) {
        Reservation reservation = reservationDao.findById(id);
        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationDao.findByUserName(username).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void update(Long reservationId, LocalDateTime now, ReservationUpdateRequest request) {
        try {
            Time time = timeDao.findById(request.timeId());
            LocalDateTime targetDateTime = LocalDateTime.of(request.targetDate(), time.getStartAt());
            validateDateAndTimeNotPast(now,targetDateTime);
            reservationDao.updateDateAndTimeById(reservationId,request.targetDate(), time.getId());
        } catch (DuplicateKeyException e){
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void delete(LocalDateTime now, Long id) {
        Reservation reservation = reservationDao.findById(id);
        LocalDateTime localDateTime = LocalDateTime.of(reservation.getDate(), reservation.getTime().getStartAt());
        if (now.isAfter(localDateTime )) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);
        }
        reservationDao.delete(id);
    }

    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
        if (now.isAfter(reservationTime)) {
            throw new CustomException(ErrorCode.PAST_DATE_RESERVATION);
        }
    }
}
