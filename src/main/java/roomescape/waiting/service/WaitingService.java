package roomescape.waiting.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.exception.PastReservationWaitingCancellationException;
import roomescape.waiting.domain.exception.WaitingNotFoundException;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.repository.dto.WaitingWithRank;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final Clock clock;

    @Transactional
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

        return waitingRepository.save(waiting);
    }

    @Transactional
    public void deleteByIdAndCustomerName(final long waitingId, final String customerName) {
        final Waiting waiting = getWaitingById(waitingId);

        if (!waiting.isOwnedBy(customerName)) {
            throw new WaitingNotFoundException();
        }
        if (!waiting.isCancelable(LocalDateTime.now(clock))) {
            throw new PastReservationWaitingCancellationException();
        }

        deleteWaiting(waitingId);
    }

    @Transactional
    public void deleteByIdForPromotion(final long waitingId) {
        deleteWaiting(waitingId);
    }

    @Transactional(readOnly = true)
    public Waiting getWaitingById(final long waitingId) {
        return waitingRepository.findById(waitingId)
            .orElseThrow(WaitingNotFoundException::new);
    }

    @Transactional
    public Optional<Waiting> findEarliestWaitingBySlot(
        final LocalDate reservationDate,
        final long timeId,
        final long themeId
    ) {
        return waitingRepository.findEarliestBySlotForUpdate(reservationDate, timeId, themeId);
    }

    @Transactional(readOnly = true)
    public List<WaitingWithRank> findAllWithRankByCustomerNameAfterNow(final String customerName) {
        return waitingRepository.findAllWithRankByCustomerNameAndReservationDateTimeAfter(
            customerName,
            LocalDateTime.now(clock)
        );
    }

    @Transactional(readOnly = true)
    public List<WaitingWithRank> findAllWithRank() {
        return waitingRepository.findAllWithRank();
    }

    @Transactional(readOnly = true)
    public boolean existsBySlot(
        final LocalDate reservationDate,
        final long timeId,
        final long themeId
    ) {
        return waitingRepository.existsBySlot(reservationDate, timeId, themeId);
    }

    private void deleteWaiting(final long waitingId) {
        final boolean deleted = waitingRepository.deleteById(waitingId);
        if (!deleted) {
            throw new WaitingNotFoundException();
        }
    }
}
