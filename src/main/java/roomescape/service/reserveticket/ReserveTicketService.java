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
import roomescape.exception.reservation.InvalidReservationException;
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

    public List<ReserveTicketWaiting> memberReservationWaitingTickets(Long memberId) {
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

    @Transactional
    public long addWaitingReservation(AddReservationDto addReservationDto, Long reserverId) {
        long reservationId = reservationService.addReservation(addReservationDto, new NonDuplicateCheckStrategy(),
                ReservationStatus.PREPARE);

        validateIsReservationDoesntExist(addReservationDto, reserverId);
        validateIsSameWaitingExist(addReservationDto, reserverId);

        Reservation reservation = reservationService.getReservationById(reservationId);
        Reserver reserver = memberService.getMemberById(reserverId);
        ReserveTicket reserveTicket = new ReserveTicket(null, reservation, reserver);
        return reserveTicketRepository.save(reserveTicket).getId();
    }

    private void validateIsReservationDoesntExist(AddReservationDto addReservationDto, Long reserverId) {
        boolean isExistBySameReservation = reserveTicketRepository.existsBySameReservation(addReservationDto.themeId(),
                addReservationDto.date(),
                addReservationDto.timeId(), reserverId, ReservationStatus.RESERVATION);

        if (!isExistBySameReservation) {
            throw new InvalidReservationException("예약이 존재하지 않습니다");
        }
    }

    private void validateIsSameWaitingExist(AddReservationDto addReservationDto, Long reserverId) {
        if (reserveTicketRepository.existsBySameReservation(addReservationDto.themeId(),
                addReservationDto.date(),
                addReservationDto.timeId(), reserverId, ReservationStatus.PREPARE)) {
            throw new InvalidReservationException("중복된 대기 요청입니다.");
        }
    }
}
