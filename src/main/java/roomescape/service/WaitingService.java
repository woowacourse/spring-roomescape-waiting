package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.service.dto.WaitingRequest;
import roomescape.service.dto.WaitingResponse;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResponse save(WaitingRequest waitingRequest, Member member) {
        ReservationTime reservationTime = reservationTimeRepository.findReservationTimeById(waitingRequest.getTime());
        Theme theme = themeRepository.findThemeById(waitingRequest.getTheme());
        Waiting waiting = waitingRequest.toWaiting(member, reservationTime, theme);

        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }
}
