package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class AdminReservationService {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public AdminReservationService(final ReservationService reservationService, final MemberRepository memberRepository,
                                   final ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse save(final AdminReservationSaveRequest adminReservationSaveRequest) {
        Member member = findMemberById(adminReservationSaveRequest.memberId());
        return reservationService.save(adminReservationSaveRequest.toReservationSaveRequest(), member);
    }

    private Member findMemberById(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 회원 번호를 입력하였습니다."));
    }

    public List<ReservationResponse> getByFilter(final Long memberId, final Long themeId,
                                                 final LocalDate dateFrom, final LocalDate dateTo) {
        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
