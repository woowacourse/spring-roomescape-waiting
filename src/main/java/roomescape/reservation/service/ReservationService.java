package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.domain.*;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final CommonFindService commonFindService;
    private final ReservationRepository reservationRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(CommonFindService commonFindService, ReservationRepository reservationRepository, MemberReservationRepository memberReservationRepository) {
        this.commonFindService = commonFindService;
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<MyReservationWithStatus> findMyReservations(AuthInfo authInfo) {
        Member member = commonFindService.getMember(authInfo.getId());
        return memberReservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationWithStatus::from)
                .toList();
    }

    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservation_Id(reservationId);
        reservationRepository.deleteById(reservationId);
    }
}
