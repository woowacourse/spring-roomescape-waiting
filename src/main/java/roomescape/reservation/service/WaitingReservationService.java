package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.RoomEscapeInformation;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingReservationRequest;
import roomescape.reservation.dto.WaitingReservationResponse;
import roomescape.reservation.repository.RoomEscapeInformationRepository;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final RoomEscapeInformationRepository roomEscapeInformationRepository;


    @Transactional
    public WaitingReservationResponse save(final WaitingReservationRequest request, final LoginMember loginMember) {
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다."));
        final Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버입니다."));
        final RoomEscapeInformation roomEscapeInformation = RoomEscapeInformation.builder()
                .date(request.date())
                .theme(theme)
                .time(reservationTime)
                .build();
        roomEscapeInformationRepository.save(roomEscapeInformation);

        final WaitingReservation waitingReservation = WaitingReservation.builder()
                .roomEscapeInformation(roomEscapeInformation)
                .member(member)
                .build();
        return new WaitingReservationResponse(waitingReservationRepository.save(waitingReservation));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationResponse saveAsWaitingIfReserved(final WaitingReservation waitingReservation) {
        return new ReservationResponse(waitingReservationRepository.save(waitingReservation));
    }

    public WaitingReservation findWaitingReservationById(final Long id) {
        return waitingReservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기입니다."));
    }

    public void deleteById(final Long id) {
        this.waitingReservationRepository.deleteById(id);
    }
}
