package roomescape.wating.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.domain.exception.PastReservationWaitingCancellationException;
import roomescape.wating.domain.exception.WaitingNotFoundException;
import roomescape.wating.domain.exception.WaitingSlotDuplicateException;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.service.dto.request.WaitingCreateRequest;
import roomescape.wating.service.dto.response.WaitingCreateResponse;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public WaitingCreateResponse create(final WaitingCreateRequest request) {
        final Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(ThemeNotFoundException::new);
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(ReservationTimeNotFoundException::new);

        final Waiting waiting = Waiting.create(
            request.name(),
            request.date(),
            reservationTime,
            theme,
            LocalDateTime.now(clock)
        );
        try {
            return new WaitingCreateResponse(waitingRepository.save(waiting));
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
