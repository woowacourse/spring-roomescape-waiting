package roomescape.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationTicketRegisterDto;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.dto.response.ReservationTicketResponseDto;
import roomescape.model.Member;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.persistence.repository.WaitingRepository;
import roomescape.persistence.vo.Period;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationTicketResponseDto saveReservation(ReservationTicketRegisterDto reservationTicketRegisterDto,
                                                        LoginMember loginMember) {
        ReservationTicket reservationTicket = createReservation(reservationTicketRegisterDto, loginMember);
        assertReservationIsNotDuplicated(reservationTicket);

        ReservationTicket savedReservationTicket = reservationRepository.save(reservationTicket);
        return new ReservationTicketResponseDto(savedReservationTicket);
    }

    public List<ReservationTicketResponseDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationTicketResponseDto::new)
                .toList();
    }

    public List<ReservationTicketResponseDto> searchReservations(ReservationSearchDto reservationSearchDto) {
        Long themeId = reservationSearchDto.themeId();
        Long memberId = reservationSearchDto.memberId();
        LocalDate startDate = reservationSearchDto.startDate();
        LocalDate endDate = reservationSearchDto.endDate();

        return reservationRepository.findForThemeAndMemberInPeriod(
                        themeId,
                        memberId,
                        new Period(startDate, endDate)
                ).stream()
                .map(ReservationTicketResponseDto::new)
                .toList();
    }

    public void cancelReservation(Long id) {
        ReservationTicket reservationTicket = reservationRepository.findById(id);
        reservationRepository.deleteById(id);

        promoteNextWaitingToReservation(reservationTicket);
    }

    private void promoteNextWaitingToReservation(ReservationTicket reservationTicket) {
        Optional<Waiting> optionalNextWaiting = waitingRepository.findNextWaiting(
                reservationTicket.getDate(),
                reservationTicket.getReservationTime(),
                reservationTicket.getTheme()
        );

        if (optionalNextWaiting.isEmpty()) {
            return;
        }

        Waiting nextWaiting = optionalNextWaiting.get();

        promoteToReservation(nextWaiting);
        waitingRepository.delete(nextWaiting);
    }

    private void promoteToReservation(Waiting nextWaiting) {
        ReservationTicket convertedReservationTicket = convertToReservation(nextWaiting);
        reservationRepository.save(convertedReservationTicket);
    }

    public List<MemberReservationResponseDto> getReservationsOfMember(LoginMember loginMember) {
        List<ReservationTicket> reservationTickets = reservationRepository.findForMember(loginMember.id());

        return reservationTickets.stream()
                .map(MemberReservationResponseDto::new)
                .toList();
    }

    private ReservationTicket createReservation(ReservationTicketRegisterDto reservationTicketRegisterDto, LoginMember loginMember) {
        ReservationTime time = reservationTimeRepository.findById(reservationTicketRegisterDto.timeId());
        Theme theme = themeRepository.findById(reservationTicketRegisterDto.themeId());
        Member member = memberRepository.findById(loginMember.id());

        return reservationTicketRegisterDto.convertToReservation(time, theme, member);
    }

    private void assertReservationIsNotDuplicated(ReservationTicket reservationTicket) {
        if (reservationRepository.isDuplicatedForDateAndReservationTime(reservationTicket.getDate(),
                reservationTicket.getReservationTime())) {
            throw new DuplicatedException("이미 예약이 존재합니다.");
        }
    }

    private ReservationTicket convertToReservation(Waiting nextWaiting) {
        return new ReservationTicket(
                nextWaiting.getReservationDate(),
                nextWaiting.getReservationTime(),
                nextWaiting.getTheme(),
                nextWaiting.getPendingReservation().getMember(),
                nextWaiting.getRegisteredAt().toLocalDate()
        );
    }
}
