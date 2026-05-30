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

        ReservationWaiting reservationWaitingCommand = reservationWaitingReq.to(reservationTimeById, themeById);
        reservationWaitingCommand.validatePastDateTime();

        Reservation reservation = getReservationByThemeAndDateAndTime(reservationWaitingReq.themeId(), reservationWaitingReq.date(), reservationWaitingReq.timeId());
        reservation.validateDuplicatedReservationByName(reservationWaitingReq.name());

        if(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId(reservationWaitingReq.name(), reservationWaitingReq.date(), reservationWaitingReq.timeId(), reservationWaitingReq.themeId())) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }

        ReservationWaiting reservationWaiting = reservationWaitingReq.to(reservationTimeById, themeById);
        Long id = reservationWaitingUpdatingDao.create(reservationWaiting);

        return ReservationWaitingResponse.from(reservationWaitingQueryingDao.findReservationWaitingById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 대기열이 존재하지 않습니다.")));
    }

    public void delete(Long id) {
        ReservationWaiting reservationWaiting =  reservationWaitingQueryingDao.findReservationWaitingById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 대기열이 존재하지 않습니다."));

        reservationWaiting.validatePastDateTime();

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
