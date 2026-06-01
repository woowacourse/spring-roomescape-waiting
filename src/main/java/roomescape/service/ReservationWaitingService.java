package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryDao;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationWaitingQueryDao;
import roomescape.repository.ReservationWaitingUpdateDao;
import roomescape.repository.ThemeQueryDao;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingUpdateDao reservationWaitingUpdatingDao;
    private final ReservationWaitingQueryDao reservationWaitingQueryingDao;
    private final ReservationQueryDao reservationQueryingDao;
    private final ReservationTimeQueryDao reservationTimeQueryingDao;
    private final ThemeQueryDao themeQueryingDao;

    public ReservationWaitingService(ReservationWaitingUpdateDao reservationWaitingUpdatingDao, ReservationWaitingQueryDao reservationWaitingQueryingDao, ReservationQueryDao reservationQueryingDao, ReservationTimeQueryDao reservationTimeQueryingDao, ThemeQueryDao themeQueryingDao) {
        this.reservationWaitingUpdatingDao = reservationWaitingUpdatingDao;
        this.reservationWaitingQueryingDao = reservationWaitingQueryingDao;
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
    }

    public ReservationWaitingResponse create(ReservationWaitingRequest reservationWaitingReq) {
        ReservationTime reservationTimeById = reservationTimeQueryingDao.findReservationTimeById(reservationWaitingReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationWaitingReq.timeId()));
        Theme themeById = themeQueryingDao.findThemeById(reservationWaitingReq.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationWaitingReq.themeId()));

        ReservationSlot reservationSlot = new ReservationSlot(reservationWaitingReq.date(), reservationTimeById, themeById);

        Reservation reservation = getReservationBySlot(reservationSlot);
        reservation.validateWaitable(reservationWaitingReq.name());

        if(reservationWaitingQueryingDao.isExistByNameAndSlot(reservationWaitingReq.name(), reservationSlot)) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }

        ReservationWaiting reservationWaiting = reservationWaitingReq.to(reservationTimeById, themeById);
        Long id = reservationWaitingUpdatingDao.create(reservationWaiting);

        return ReservationWaitingResponse.from(reservationWaitingQueryingDao.findReservationWaitingById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id)));
    }

    public void delete(Long id, String name) {
        ReservationWaiting reservationWaiting =  reservationWaitingQueryingDao.findReservationWaitingById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id));

        reservationWaiting.validateOwner(name);
        reservationWaiting.validatePastDateTime();

        reservationWaitingUpdatingDao.delete(id);
    }

    public List<ReservationWaitingResponse> readAll() {
        return reservationWaitingQueryingDao.findAllReservationWaiting()
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    private Reservation getReservationBySlot(ReservationSlot reservationSlot) {
        return reservationQueryingDao.findReservationBySlot(reservationSlot).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));
    }
}
