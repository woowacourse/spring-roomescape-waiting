package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationWaitingQueryingDao;
import roomescape.repository.ReservationWaitingUpdatingDao;
import roomescape.repository.ThemeQueryingDao;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingUpdatingDao reservationWaitingUpdatingDao;
    private final ReservationWaitingQueryingDao reservationWaitingQueryingDao;
    private final ReservationQueryingDao reservationQueryingDao;
    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ThemeQueryingDao themeQueryingDao;

    public ReservationWaitingService(ReservationWaitingUpdatingDao reservationWaitingUpdatingDao, ReservationWaitingQueryingDao reservationWaitingQueryingDao, ReservationQueryingDao reservationQueryingDao, ReservationTimeQueryingDao reservationTimeQueryingDao, ThemeQueryingDao themeQueryingDao) {
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

        ReservationWaiting reservationWaitingCommand = reservationWaitingReq.to(reservationTimeById, themeById);

        Reservation reservation = getReservationByThemeAndDateAndTime(reservationWaitingReq.themeId(), reservationWaitingReq.date(), reservationWaitingReq.timeId());

        if(reservation.isReservedBy(reservationWaitingCommand.getName())) {
            throw new InvalidInputException("이미 등록된 예약이 있습니다.");
        }

        if(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId(reservationWaitingReq.name(), reservationWaitingReq.date(), reservationWaitingReq.timeId(), reservationWaitingReq.themeId())) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }

        ReservationWaiting reservationWaiting = reservationWaitingReq.to(reservationTimeById, themeById);
        Long id = reservationWaitingUpdatingDao.create(reservationWaiting);

        return ReservationWaitingResponse.from(reservationWaitingQueryingDao.findReservationWaitingById(id).get());
    }

    public void delete(Long id) {
        ReservationWaiting reservationWaiting =  reservationWaitingQueryingDao.findReservationWaitingById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 대기열이 존재하지 않습니다."));

        reservationWaitingUpdatingDao.delete(id);
    }

    public List<ReservationWaitingResponse> readAll() {
        return reservationWaitingQueryingDao.findAllReservationWaiting()
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    public List<ReservationWaitingResponse> readByName(String name) {
        return reservationWaitingQueryingDao.findAllByName(name)
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    private Reservation getReservationByThemeAndDateAndTime(Long themeId, LocalDate date, Long timeId) {
        return reservationQueryingDao.findReservationByThemeAndDateAndTime(themeId, date, timeId).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));
    }
}
