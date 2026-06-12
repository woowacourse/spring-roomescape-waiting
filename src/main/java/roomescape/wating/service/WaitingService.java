package roomescape.wating.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableContentException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.controller.dto.request.WaitingCreateRequest;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private static final String WAITING_NOT_FOUND_MESSAGE = "존재하지 않는 대기입니다.";
    private static final String THEME_NOT_FOUND_MESSAGE = "존재하지 않는 테마입니다.";
    private static final String RESERVATION_TIME_NOT_FOUND_MESSAGE = "존재하지 않는 예약 시간입니다.";
    private static final String NO_RESERVATION_FOR_WAITING_MESSAGE = "예약이 존재하지 않는 슬롯에는 대기를 신청할 수 없습니다.";
    private static final String WAITING_SLOT_DUPLICATE_MESSAGE = "해당 시간에 이미 대기가 존재합니다.";
    private static final String PAST_RESERVATION_WAITING_CANCELLATION_MESSAGE = "과거 시간 예약의 대기를 삭제할 수 없습니다.";

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    @Transactional
    public long create(final WaitingCreateRequest request) {
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException(THEME_NOT_FOUND_MESSAGE));
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException(RESERVATION_TIME_NOT_FOUND_MESSAGE));
        final ReservationSlot slot = reservationSlotRepository.findByDateAndTimeIdAndThemeIdForUpdate(
                request.date(),
                reservationTime.getId(),
                theme.getId()
        ).orElseThrow(() -> new UnprocessableContentException(NO_RESERVATION_FOR_WAITING_MESSAGE));

        final Waiting waiting = Waiting.create(
                request.name(),
                request.email(),
                slot,
                LocalDateTime.now()
        );
        try {
            return waitingRepository.save(waiting);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException(WAITING_SLOT_DUPLICATE_MESSAGE, exception);
        }
    }

    @Transactional
    public void deleteByIdAndCustomer(final long waitingId, final String customerName, final String customerEmail) {
        final Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException(WAITING_NOT_FOUND_MESSAGE));

        reservationSlotRepository.findByIdForUpdate(waiting.getSlotId())
                .orElseThrow(() -> new NotFoundException(WAITING_NOT_FOUND_MESSAGE));

        final Waiting lockedWaiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException(WAITING_NOT_FOUND_MESSAGE));
        validateCancelableByCustomer(lockedWaiting, customerName, customerEmail);

        if (!waitingRepository.deleteById(waitingId)) {
            throw new NotFoundException(WAITING_NOT_FOUND_MESSAGE);
        }
    }

    private void validateCancelableByCustomer(
            final Waiting waiting,
            final String customerName,
            final String customerEmail
    ) {
        if (!waiting.isOwnedBy(customerName, customerEmail)) {
            throw new NotFoundException(WAITING_NOT_FOUND_MESSAGE);
        }
        if (!waiting.isCancelable(LocalDateTime.now())) {
            throw new UnprocessableContentException(PAST_RESERVATION_WAITING_CANCELLATION_MESSAGE);
        }
    }
}
