package roomescape.service.reservationmember;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMember;
import roomescape.dto.reservation.AddReservationDto;
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
        return reservationMemberRepository.findAll();
    }

    @Transactional
    public void deleteReservation(long id) {
        reservationMemberRepository.deleteById(id);
    }

    public List<ReservationMember> searchReservations(Long themeId, Long memberId, LocalDate dateFrom,
                                                      LocalDate dateTo) {
        List<ReservationMember> reservationMembers = reservationMemberRepository.findAllByMemberId(memberId);

        reservationMembers.removeIf(reservationMember ->
                reservationService.searchReservation(
                        reservationMember.getReservationId(), themeId, dateFrom, dateTo
                ).isPresent()
        );

        return reservationMembers;
    }

    public List<ReservationMember> memberReservations(long memberId) {
        return reservationMemberRepository.findAllByMemberId(memberId);
    }
}
