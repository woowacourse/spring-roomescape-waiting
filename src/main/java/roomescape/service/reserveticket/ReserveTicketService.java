package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.repository.reserveticket.ReserveTicketRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.waiting.WaitingService;

@Service
public class ReserveTicketService {

    private final ReserveTicketRepository reserveTicketRepository;
    private final MemberService memberService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReserveTicketService(ReserveTicketRepository reserveTicketRepository,
                                MemberService memberService,
                                ReservationService reservationService,
                                WaitingService waitingService) {
        this.reserveTicketRepository = reserveTicketRepository;
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    public long addReservation(AddReservationDto newReservationDto, long memberId) {
        validateIfWaitingExists(newReservationDto);
        Member member = memberService.getMemberById(memberId);
        long reservationId = reservationService.addReservation(newReservationDto, member.getName());
        Reservation reservation = reservationService.getReservationById(reservationId);
        return reserveTicketRepository.add(reservation, member);
    }

    private void validateIfWaitingExists(AddReservationDto newReservation) {
        if (waitingService.existsByDateAndTimeAndTheme(newReservation.date(), newReservation.timeId(), newReservation.themeId())) {
            throw new IllegalArgumentException("이미 대기 중인 예약이 존재합니다. 예약대기 기능을 사용해 주세요.");
        }
    }

    public List<ReserveTicket> allReservations() {
        return reserveTicketRepository.findAll();
    }

    public void deleteReservation(long id) {
        reserveTicketRepository.deleteById(id);
    }

    public List<ReserveTicket> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                  LocalDate dateTo) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAllByMemberId(memberId);

        reserveTickets.removeIf(reservationMember ->
                reservationService.searchReservation(
                        reservationMember.getReservationId(), themeId, dateFrom, dateTo
                ).isEmpty()
        );

        return reserveTickets;
    }

    public ReservationSlotTimes availableReservationTimes(AvailableTimeRequestDto availableTimeRequestDto) {
        List<ReservationTime> times = reservationService.getAllReservationTimes();
        List<Reservation> alreadyReservedReservations = reservationService.getAllByDateAndThemeId(availableTimeRequestDto.date(), availableTimeRequestDto.themeId());
        List<Waiting> alreadyWaitings = waitingService.getAllByDateAndThemeId(availableTimeRequestDto.date(), availableTimeRequestDto.themeId());
        return new ReservationSlotTimes(times, alreadyReservedReservations, alreadyWaitings);
    }

    public List<ReserveTicket> memberReservations(long memberId) {
        return reserveTicketRepository.findAllByMemberId(memberId);
    }
}
