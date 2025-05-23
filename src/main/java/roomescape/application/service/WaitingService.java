package roomescape.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRegisterDto;
import roomescape.dto.response.WaitingResponseDto;
import roomescape.model.Member;
import roomescape.model.PendingReservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.persistence.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingResponseDto registerWaiting(LoginMember loginMember, WaitingRegisterDto waitingRegisterDto) {
        Member member = memberRepository.findById(loginMember.id());
        ReservationTime reservationTime = reservationTimeRepository.findById(waitingRegisterDto.time());
        Theme theme = themeRepository.findById(waitingRegisterDto.theme());

        PendingReservation pendingReservation = new PendingReservation(waitingRegisterDto.date(), reservationTime,
                theme, member, LocalDate.now());
        Waiting waiting = new Waiting(LocalDateTime.now(), pendingReservation);

        Waiting savedWaiting = waitingRepository.save(waiting);

        return new WaitingResponseDto(savedWaiting.getId());
    }
}
