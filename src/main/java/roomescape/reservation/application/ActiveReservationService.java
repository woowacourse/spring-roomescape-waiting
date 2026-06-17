package roomescape.reservation.application;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.application.exception.ReservationAlreadyChangedException;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.ActiveReservationRepository;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.infra.exception.ReservationAlreadyCancelledException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActiveReservationService {

    private final Clock clock;
    private final ActiveReservationRepository reservationRepository;

    @Transactional(propagation = Propagation.NESTED)
    public ReservationInfo add(final TimeSlot slot, final ReservationCreateCommand command) {
        if (reservationRepository.existsByActiveSlotId(slot.getId())) {
            throw new ReservationInUseException("이미 확정 예약이 존재합니다.");
        }
        try {
            ActiveReservation reservation = command.toActiveEntity(slot, clock);
            return ReservationInfo.from(reservationRepository.save(reservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("이미 확정 예약이 존재합니다.");
        }
    }

    public ReservationInfo transferReservation(final Long id, final TimeSlot slot, final ReservationCreateCommand command) {
        if (reservationRepository.existsByActiveSlotId(slot.getId())) {
            throw new ReservationInUseException("이미 확정 예약이 존재합니다.");
        }
        try {
            ActiveReservation reservation = command.toActiveEntity(slot, clock);
            ActiveReservation activeReservation = reservation.withId(id);
            return ReservationInfo.from(reservationRepository.insertWithId(activeReservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("이미 확정 예약이 존재합니다.");
        }
    }

    @Transactional
    public Long cancel(final Long id, final String name) {
        ActiveReservation reservation = reservationRepository.getById(id);
        ActiveReservation cancelled = reservation.cancel(name, clock);
        int affected = reservationRepository.cancel(cancelled);
        if (affected == 0) {
            throw new ReservationAlreadyCancelledException("이미 취소된 예약입니다.");
        }
        return reservation.getSlot().getId();
    }

    @Transactional
    public void savePromoted(final ActiveReservation promotedReservation) {
        try {
            reservationRepository.insertWithId(promotedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("예약 승격 중 일시적인 문제가 발생했습니다.");
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    public ReservationInfo change(final Long id, final TimeSlot slot, final String name) {
        try {
            ActiveReservation reservation = reservationRepository.getById(id);
            ActiveReservation changedReservation = reservation.changeTime(name, slot, clock);
            int affected = reservationRepository.update(changedReservation);
            if(affected == 0) {
                throw new ReservationAlreadyChangedException("이미 변경된 예약입니다.");
            }
            return ReservationInfo.from(changedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("변경 하려는 시간에 이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public boolean existsBySlotId(Long slotId, Long id) {
        return reservationRepository.existsByActiveSlotIdNotId(slotId, id);
    }

    @Transactional
    public boolean existsBySlotId(Long slotId) {
        return reservationRepository.existsByActiveSlotId(slotId);
    }

    @Transactional
    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    public Long getSlotId(final Long id) {
        return reservationRepository.getById(id)
                .getSlot()
                .getId();
    }

    public List<ReservationInfo> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationInfo::from)
                .toList();
    }

    public List<ReservationPendingInfo> getReservationsByName(final String name) {
        return reservationRepository.findAllByName(name)
                .stream()
                .map(ReservationPendingInfo::from)
                .toList();
    }
}
