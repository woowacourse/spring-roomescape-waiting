package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.RoomEscapeInformation;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.dto.WaitingReservationRequest;
import roomescape.reservation.dto.WaitingReservationResponse;
import roomescape.reservation.repository.RoomEscapeInformationRepository;
import roomescape.reservation.repository.WaitingReservationRepository;

@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final MemberRepository memberRepository;
    private final RoomEscapeInformationRepository roomEscapeInformationRepository;
    
    @Transactional
    public WaitingReservationResponse save(final WaitingReservationRequest request, final LoginMember loginMember) {
        final Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버입니다."));
        final RoomEscapeInformation roomEscapeInformation = roomEscapeInformationRepository.findByDateAndTimeIdAndThemeId(
                        request.date(), request.timeId(), request.themeId())
                .orElseThrow(() -> new NotFoundException("방탈출 정보가 존재하지 않습니다."));

        final WaitingReservation waitingReservation = WaitingReservation.builder()
                .roomEscapeInformation(roomEscapeInformation)
                .member(member)
                .build();
        return new WaitingReservationResponse(waitingReservationRepository.save(waitingReservation));
    }

    public void deleteById(final Long id) {
        this.waitingReservationRepository.deleteById(id);
    }
}
