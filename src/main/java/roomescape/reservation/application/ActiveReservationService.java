package roomescape.reservation.application;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.ActiveReservationRepository;
import roomescape.reservation.domain.TimeSlot;

@Service
@RequiredArgsConstructor
public class ActiveReservationService {

    private final Clock clock;
    private final ActiveReservationRepository reservationRepository;

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

    public Long cancel(final Long id, final ReservationCancelCommand command) {
        ActiveReservation reservation = reservationRepository.getById(id);
        ActiveReservation cancelled = reservation.cancel(command.name(), clock);
        reservationRepository.cancel(cancelled);
        return reservation.getSlot().getId();
    }

    public void savePromoted(final ActiveReservation promotedReservation) {
        try {
            reservationRepository.save(promotedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("예약 승격 중 일시적인 문제가 발생했습니다.");
        }
    }

    public ReservationInfo change(final Long id, final TimeSlot slot, final ReservationChangeCommand command) {
        ActiveReservation reservation = reservationRepository.getById(id);
        ActiveReservation changedReservation = reservation.changeTime(command.name(), slot, clock);
        reservationRepository.update(changedReservation);
        return ReservationInfo.from(changedReservation);
    }

    public boolean existsBySlotId(Long id) {
        return reservationRepository.existsByActiveSlotId(id);
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
