package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.SlotDomainService;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.ConcurrencyConflictException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationWaitingDao;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingDao reservationWaitingDao;
    private final SlotDomainService slotDomainService;
    private final ReservationQueryingDao reservationQueryingDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao, SlotDomainService slotDomainService,
                                     ReservationQueryingDao reservationQueryingDao) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.slotDomainService = slotDomainService;
        this.reservationQueryingDao = reservationQueryingDao;
    }

    @Transactional
    public ReservationWaitingResponse create(ReservationWaitingRequest reservationWaitingReq) {
        Slot slot = slotDomainService.find(reservationWaitingReq.date(), reservationWaitingReq.timeId(), reservationWaitingReq.themeId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));

        Reservation current = reservationQueryingDao.findReservationBySlotId(slot.getId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));
        if (current.isReservedBy(reservationWaitingReq.name())) {
            throw new InvalidInputException("이미 등록된 예약이 있습니다.");
        }

        ReservationWaiting reservationWaitingCommand = ReservationWaiting.create(reservationWaitingReq.name(), slot);

        if (reservationWaitingDao.isExistByNameAndSlotId(reservationWaitingReq.name(), slot.getId())) {
            throw new InvalidInputException("이미 대기열에 등록되어 있습니다.");
        }
        Long id = reservationWaitingDao.create(reservationWaitingCommand);
        ReservationWaiting reservationWaiting = reservationWaitingDao.findReservationWaitingById(id)
                .orElseThrow(() -> new ConcurrencyConflictException("대기열 생성 과정에서 오류가 발생했습니다."));

        return ReservationWaitingResponse.from(reservationWaiting);


    }

    public void delete(Long id) {
        reservationWaitingDao.delete(id);
    }

    public List<ReservationWaitingResponse> readAll() {
        return reservationWaitingDao.findAllReservationWaiting()
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    public List<ReservationWaitingResponse> readByName(String name) {
        return reservationWaitingDao.findAllByName(name)
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }
}
