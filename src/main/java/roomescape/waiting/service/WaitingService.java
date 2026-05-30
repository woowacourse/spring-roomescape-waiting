package roomescape.waiting.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.exception.NoReservationForWaitingException;
import roomescape.waiting.domain.exception.PastReservationWaitingCancellationException;
import roomescape.waiting.domain.exception.WaitingNotFoundException;
import roomescape.waiting.domain.exception.WaitingSlotDuplicateException;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
class WaitingService {

    private final WaitingRepository waitingRepository;
    private final Clock clock;

    public Waiting create(
        final String customerName,
        final LocalDate reservationDate,
        final ReservationTime reservationTime,
        final Theme theme
    ) {
        final Waiting waiting = Waiting.create(
            customerName,
            reservationDate,
            reservationTime,
            theme,
            LocalDateTime.now(clock)
        );

        try {
            return waitingRepository.save(waiting)
                .orElseThrow(NoReservationForWaitingException::new);
        } catch (DuplicateKeyException exception) {
            throw new WaitingSlotDuplicateException();
        }
    }

    public void deleteByIdAndCustomerName(final long waitingId, final String customerName) {
        final Waiting waiting = waitingRepository.findById(waitingId)
            .orElseThrow(WaitingNotFoundException::new);

        if (!waiting.isOwnedBy(customerName)) {
            throw new WaitingNotFoundException();
        }
        if (!waiting.isCancelable(LocalDateTime.now(clock))) {
            throw new PastReservationWaitingCancellationException();
        }

        waitingRepository.deleteById(waitingId);
    }
}
