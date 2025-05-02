package roomescape.service.reservationmember;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMember;
import roomescape.domain.reservationmember.ReservationMemberIds;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reservationmember.ReservationMemberRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;

@Service
public class ReservationMemberService {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReservationMemberRepository reservationMemberRepository;

    public ReservationMemberService(MemberService memberService, ReservationService reservationService,
                                    ReservationMemberRepository reservationMemberRepository) {
        this.memberService = memberService;
        this.reservationService = reservationService;
        this.reservationMemberRepository = reservationMemberRepository;
    }

    @Transactional
    public long addReservation(AddReservationDto newReservationDto, long memberId) {
        Member member = memberService.getMemberById(memberId);
        long reservationId = reservationService.addReservation(newReservationDto);
        Reservation reservation = reservationService.getReservationById(reservationId);
        return reservationMemberRepository.add(reservation, member);
    }

    public List<ReservationMember> allReservations() {
        List<ReservationMemberIds> reservationMembers = reservationMemberRepository.findAll();
        return reservationMembers.stream()
                .map(reservationMemberIds -> {
                    Reservation reservation = reservationService.getReservationById(
                            reservationMemberIds.getReservationId());
                    Member member = memberService.getMemberById(reservationMemberIds.getMemberId());
                    return new ReservationMember(reservation, member);
                })
                .toList();
    }

    @Transactional
    public void deleteReservation(long id) {
        ReservationMemberIds reservationMemberIds = reservationMemberRepository.findById(id)
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 예약입니다"));

        reservationMemberRepository.deleteById(id);

        long reservationId = reservationMemberIds.getReservationId();
        reservationService.deleteReservation(reservationId);
    }

    public List<ReservationMember> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                      LocalDate dateTo) {
        Member member = memberService.getMemberById(memberId);

        List<ReservationMemberIds> memberIds = reservationMemberRepository.findAllByMemberId(memberId);
        List<Long> reservationIds = memberIds.stream()
                .map(ReservationMemberIds::getReservationId)
                .toList();

        List<Reservation> searchResultReservations = new ArrayList<>();
        for (long reservationId : reservationIds) {
            Optional<Reservation> reservation = reservationService.searchReservation(reservationId, themeId, dateFrom,
                    dateTo);
            reservation.ifPresent(searchResultReservations::add);
        }

        return searchResultReservations.stream()
                .map((reservation) -> new ReservationMember(reservation, member))
                .toList();
    }
}
