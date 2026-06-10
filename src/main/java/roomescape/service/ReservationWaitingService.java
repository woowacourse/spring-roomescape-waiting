package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.dto.reservationWaiting.ReservationWaitingSequence;
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

    private final ReservationWaitingUpdateDao reservationWaitingUpdateDao;
    private final ReservationWaitingQueryDao reservationWaitingQueryDao;
    private final ReservationQueryDao reservationQueryDao;
    private final ReservationTimeQueryDao reservationTimeQueryDao;
    private final ThemeQueryDao themeQueryDao;

    public ReservationWaitingService(ReservationWaitingUpdateDao reservationWaitingUpdateDao, ReservationWaitingQueryDao reservationWaitingQueryDao, ReservationQueryDao reservationQueryDao, ReservationTimeQueryDao reservationTimeQueryDao, ThemeQueryDao themeQueryDao) {
        this.reservationWaitingUpdateDao = reservationWaitingUpdateDao;
        this.reservationWaitingQueryDao = reservationWaitingQueryDao;
        this.reservationQueryDao = reservationQueryDao;
        this.reservationTimeQueryDao = reservationTimeQueryDao;
        this.themeQueryDao = themeQueryDao;
    }

    @Transactional
    public ReservationWaitingResponse create(ReservationWaitingRequest reservationWaitingReq) {
        ReservationTime reservationTimeById = reservationTimeQueryDao.findReservationTimeById(reservationWaitingReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationWaitingReq.timeId()));
        Theme themeById = themeQueryDao.findThemeById(reservationWaitingReq.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationWaitingReq.themeId()));

        ReservationSlot reservationSlot = new ReservationSlot(reservationWaitingReq.date(), reservationTimeById, themeById);

        Reservation reservation = reservationQueryDao.findReservationBySlotForUpdate(reservationSlot)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));
        reservation.validateWaitable(reservationWaitingReq.name());

        if(reservationWaitingQueryDao.isExistByNameAndSlot(reservationWaitingReq.name(), reservationSlot)) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }

        ReservationWaiting reservationWaiting = reservationWaitingReq.toReservationWaiting(reservationTimeById, themeById);
        Long id = reservationWaitingUpdateDao.create(reservationWaiting);

        ReservationWaitingSequence createdWaitingSequence = reservationWaitingQueryDao.findReservationWaitingById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id));
        return ReservationWaitingResponse.from(createdWaitingSequence.reservationWaiting(), createdWaitingSequence.sequence());
    }

    public void delete(Long id, String name) {
        ReservationWaiting reservationWaiting = reservationWaitingQueryDao.findReservationWaitingById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id))
                .reservationWaiting();

        reservationWaiting.validateOwner(name);
        reservationWaiting.validatePastDateTime();

        reservationWaitingUpdateDao.delete(id);
    }

    public List<ReservationWaitingResponse> readAll() {
        return reservationWaitingQueryDao.findAllReservationWaiting()
                .stream()
                .map(waitingSequence -> ReservationWaitingResponse.from(waitingSequence.reservationWaiting(), waitingSequence.sequence()))
                .toList();
    }
}
