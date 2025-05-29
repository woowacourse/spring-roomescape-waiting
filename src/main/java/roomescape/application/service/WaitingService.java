package roomescape.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.OperationNotAllowedException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRegisterDto;
import roomescape.dto.response.MemberWaitingResponseDto;
import roomescape.dto.response.WaitingAdminResponseDto;
import roomescape.dto.response.WaitingResponseDto;
import roomescape.model.Member;
import roomescape.model.ReservationSpec;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationTicketRepository;
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
    private final ReservationTicketRepository reservationTicketRepository;

    public WaitingResponseDto registerWaiting(LoginMember loginMember, WaitingRegisterDto waitingRegisterDto) {
        ReservationSpec reservationSpec = createReservation(loginMember, waitingRegisterDto);
        validateReservationExistsForWaiting(reservationSpec);

        Waiting waiting = new Waiting(reservationSpec);
        Waiting savedWaiting = waitingRepository.save(waiting);

        return new WaitingResponseDto(savedWaiting.getId());
    }

    private ReservationSpec createReservation(LoginMember loginMember, WaitingRegisterDto waitingRegisterDto) {
        Member member = memberRepository.findById(loginMember.id());
        ReservationTime reservationTime = reservationTimeRepository.findById(waitingRegisterDto.time());
        Theme theme = themeRepository.findById(waitingRegisterDto.theme());

        ReservationSpec reservationSpec = new ReservationSpec(
                waitingRegisterDto.date(),
                reservationTime,
                theme,
                member,
                LocalDate.now()
        );
        return reservationSpec;
    }

    private void validateReservationExistsForWaiting(ReservationSpec reservationSpec) {
        Optional<ReservationTicket> foundReservationTicket = reservationTicketRepository.findForThemeAndReservationTimeOnDate(
                reservationSpec);

        if (foundReservationTicket.isEmpty()) {
            throw new OperationNotAllowedException("예약 내역이 존재하지 않아 대기를 등록할 수 없습니다.");
        }
    }

    public void deleteWaiting(LoginMember loginMember, Long id) {
        Waiting waiting = waitingRepository.findById(id);
        Member member = memberRepository.findById(loginMember.id());

        if (!waiting.ownBy(member)) {
            throw new UnauthorizedException("해당 객체를 삭제할 수 있는 권한이 없습니다.");
        }

        waitingRepository.delete(waiting);
    }

    public List<MemberWaitingResponseDto> getMyWaitings(LoginMember loginMember) {
        List<Waiting> myWaitings = waitingRepository.findForMember(loginMember.id());

        return myWaitings.stream()
                .map(this::toMemberWaitingResponseDto)
                .toList();
    }

    public void rejectWaiting(Long id) {
        waitingRepository.rejectById(id);
    }

    public List<WaitingAdminResponseDto> getAllWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingAdminResponseDto::new)
                .collect(Collectors.toList());
    }

    private MemberWaitingResponseDto toMemberWaitingResponseDto(Waiting waiting) {
        int count = waitingRepository.countWaitingBefore(waiting);
        return new MemberWaitingResponseDto(waiting, count + 1);
    }
}

