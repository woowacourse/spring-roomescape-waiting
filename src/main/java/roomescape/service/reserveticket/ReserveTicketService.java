package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Reserver;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reserveticket.ReservationTicketWaitings;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.repository.reserveticket.ReserveTicketRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.strategy.NonDuplicateCheckStrategy;
import roomescape.service.reservation.strategy.ReservationValidateStrategy;

@Service
public class ReserveTicketService {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReserveTicketRepository reserveTicketRepository;
    private final ReservationValidateStrategy reservationRepository;

    public ReserveTicketService(MemberService memberService, ReservationService reservationService,
                                ReserveTicketRepository reserveTicketRepository,
                                ReservationValidateStrategy reservationRepository) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.reserveTicketRepository = reserveTicketRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public long addReservation(AddReservationDto newReservationDto, Long memberId) {
        Reserver reserver = memberService.getMemberById(memberId);
        long reservationId = reservationService.addReservation(newReservationDto, reservationRepository,
                ReservationStatus.RESERVATION);
        Reservation reservation = reservationService.getReservationById(reservationId);

        return reserveTicketRepository.save(
                new ReserveTicket(null, reservation, reserver)).getId();
    }

    public List<ReserveTicketWaiting> allReservationTickets() {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAll();
        ReservationTicketWaitings reservationTicketWaitings = new ReservationTicketWaitings(reserveTickets);
        return reservationTicketWaitings.reserveTicketWaitings();
    }

    public List<ReserveTicketWaiting> memberReservationTickets(Long memberId) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAllByReserverId(memberId);

        return reserveTickets.stream()
                .map((reserveTicket -> new ReserveTicketWaiting(reserveTicket.getId(), reserveTicket.getName(),
                        reserveTicket.getDate(),
                        reserveTicket.getStartAt(), reserveTicket.getReservationStatus(), 1,
                        reserveTicket.getThemeName())))
                .toList();
    }

    @Transactional
    public void deleteReservation(long id) {
        reserveTicketRepository.deleteById(id);
    }

    public List<ReserveTicketWaiting> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                         LocalDate dateTo) {
        List<ReserveTicket> reserveTickets = reserveTicketRepository.findAllByReserverId(memberId);

        reserveTickets.removeIf(reservationMember ->
                reservationService.searchReservation(
                        reservationMember.getReservationId(), themeId, dateFrom, dateTo
                ).isEmpty()
        );

        return reserveTickets.stream()
                .map((reserveTicket -> new ReserveTicketWaiting(reserveTicket.getId(), reserveTicket.getName(),
                        reserveTicket.getDate(),
                        reserveTicket.getStartAt(), reserveTicket.getReservationStatus(), 1,
                        reserveTicket.getThemeName())))
                .toList();
    }

    public List<ReserveTicket> memberReservations(Long memberId) {
        return reserveTicketRepository.findAllByReserverId(memberId);
    }

    @Transactional
    public void addWaitingReservation(AddReservationDto addReservationDto, Long memberId) {
//        int nextWeightNumber = reserveTicketRepository.countSameWaitingReservation(addReservationDto.themeId(),
//                addReservationDto.date(), addReservationDto.timeId(), minWeight) + 1;
        long reservationId = reservationService.addReservation(addReservationDto, new NonDuplicateCheckStrategy(),
                ReservationStatus.PREPARE);
        Reservation reservation = reservationService.getReservationById(reservationId);
        Reserver reserver = memberService.getMemberById(memberId);
        ReserveTicket reserveTicket = new ReserveTicket(null, reservation, reserver);
        reserveTicketRepository.save(reserveTicket);
    }
}
