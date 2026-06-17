package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.application.exception.ReservationAlreadyChangedException;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.PendingReservation;
import roomescape.reservation.domain.PendingReservationRepository;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.exception.DuplicatedReservationException;
import roomescape.reservation.infra.exception.ReservationAlreadyCancelledException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingReservationService {

    private final Clock clock;
    private final PendingReservationRepository reservationRepository;

    @Transactional
    public ReservationInfo add(TimeSlot slot, ReservationCreateCommand command) {
        if (reservationRepository.existsReservationByName(slot.getId(), command.name())) {
            throw new DuplicatedReservationException("이미 예약 대기중입니다.");
        }
        try {
            PendingReservation pendingReservation = command.toPendingEntity(slot, clock);
            return ReservationInfo.from(reservationRepository.save(pendingReservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("예약 처리 중 일시적인 문제가 발생했습니다. 다시 시도해주세요.");
        }
    }

    @Transactional
    public ReservationInfo transferReservation(final Long id, final TimeSlot slot, final ReservationCreateCommand command) {
        if (reservationRepository.existsReservationByName(slot.getId(), command.name())) {
            throw new DuplicatedReservationException("이미 예약 대기중입니다.");
        }
        try {
            PendingReservation reservation = command.toPendingEntity(slot, clock);
            PendingReservation pendingReservation = reservation.withId(id);
            return ReservationInfo.from(reservationRepository.insertWithId(pendingReservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("예약 처리 중 일시적인 문제가 방생했습니다. 다시 시도해주세요.");
        }
    }

    @Transactional
    public void cancel(final Long id, final String name) {
        PendingReservation reservation = reservationRepository.getById(id);
        PendingReservation cancelled = reservation.cancel(name, clock);
        int affected = reservationRepository.cancel(cancelled);
        if (affected == 0) {
            throw new ReservationAlreadyCancelledException("이미 취소된 예약입니다.");
        }
    }

    @Transactional
    public Optional<ActiveReservation> popNextPendingAndPromote(final Long slotId) {
        return reservationRepository.findNextPendingReservation(slotId)
                .map(pending -> {
                    PendingReservation cancelled = pending.cancel(pending.getName(), clock);
                    int affected = reservationRepository.cancel(cancelled);
                    if (affected == 0) {
                        throw new ReservationAlreadyCancelledException("승격 대상 예약이 이미 취소되었습니다.");
                    }
                    return pending.active();
                });
    }

    @Transactional
    public ReservationInfo change(Long id, TimeSlot slot, String name) {
        try {
            PendingReservation reservation = reservationRepository.getById(id);
            PendingReservation changedReservation = reservation.changeTime(name, slot, clock);
            int affected = reservationRepository.update(changedReservation);
            if (affected == 0) {
                throw new ReservationAlreadyChangedException("이미 변경된 예약입니다.");
            }
            return ReservationInfo.from(changedReservation);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("예약을 변경 중 일시적인 문제가 발생했습니다.");
        }

    }

    @Transactional
    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    public List<ReservationInfo> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationInfo::from)
                .toList();
    }

    public List<ReservationPendingInfo> getReservationsByName(String name) {
        return reservationRepository.findAllByName(name)
                .stream()
                .map(ReservationPendingInfo::from)
                .toList();
    }

    public List<PendingReservation> findExpiredReservations(LocalDate today) {
        return reservationRepository.findExpiredReservations(today);
    }
}
