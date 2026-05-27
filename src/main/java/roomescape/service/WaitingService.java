package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingCreateCommand;
import roomescape.service.dto.WaitingResult;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResult create(WaitingCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.getTimeId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeRepository.findById(command.getThemeId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 테마입니다."));

//        todo : Optional<Reservation> r = reservationRepository.findBySlot();
//        if (r.isEmpty())
//            throw new BusinessRuleViolationException(
//                    "예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요.");
//        Reservation reservation = r.get();
//        if (reservation.getName().equals(command.getName()))
//            throw error;

        if (!reservationRepository.existsByDateAndTimeAndTheme(
                command.getDate(), command.getTimeId(), command.getThemeId())) {
            throw new BusinessRuleViolationException(
                    "예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요.");
        }

        Waitings existing = new Waitings(
                waitingRepository.findBySlot(command.getDate(), time.getId(), theme.getId())
        );
        existing.validateNoDuplicateBy(command.getName());

        Waiting newWaiting = Waiting.create(
                command.getName(), command.getDate(), time, theme, existing.nextOrderIndex()
        );
        Waiting saved = waitingRepository.save(newWaiting);
        return WaitingResult.from(saved);
    }
}

