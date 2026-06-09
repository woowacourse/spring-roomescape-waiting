package roomescape.wating.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.domain.exception.NoReservationForWaitingException;
import roomescape.wating.domain.exception.PastReservationWaitingCancellationException;
import roomescape.wating.domain.exception.WaitingNotFoundException;
import roomescape.wating.domain.exception.WaitingNotOwnedException;
import roomescape.wating.domain.exception.WaitingSlotDuplicateException;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.controller.dto.request.WaitingCreateRequest;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    @Transactional
    public long create(final WaitingCreateRequest request) {
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(ThemeNotFoundException::new);
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(ReservationTimeNotFoundException::new);
        final ReservationSlot slot = reservationSlotRepository.findByDateAndTimeIdAndThemeIdForUpdate(
                request.date(),
                reservationTime.getId(),
                theme.getId()
        ).orElseThrow(NoReservationForWaitingException::new);

        final Waiting waiting = Waiting.create(
                request.name(),
                request.email(),
                slot,
                LocalDateTime.now()
        );
        try {
            return waitingRepository.save(waiting);
        } catch (DuplicateKeyException exception) {
            throw new WaitingSlotDuplicateException();
        }
    }

    @Transactional
    public void deleteByIdAndCustomer(final long waitingId, final String customerName, final String customerEmail) {
        final Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(WaitingNotFoundException::new);

        reservationSlotRepository.findByIdForUpdate(waiting.getSlotId())
                .orElseThrow(WaitingNotFoundException::new);

        final Waiting lockedWaiting = waitingRepository.findById(waitingId)
                .orElseThrow(WaitingNotFoundException::new);
        validateCancelableByCustomer(lockedWaiting, customerName, customerEmail);

        if (!waitingRepository.deleteById(waitingId)) {
            throw new WaitingNotFoundException();
        }
    }

    private void validateCancelableByCustomer(
            final Waiting waiting,
            final String customerName,
            final String customerEmail
    ) {
        if (!waiting.isOwnedBy(customerName, customerEmail)) {
            throw new WaitingNotOwnedException();
        }
        if (!waiting.isCancelable(LocalDateTime.now())) {
            throw new PastReservationWaitingCancellationException();
        }
    }
}
