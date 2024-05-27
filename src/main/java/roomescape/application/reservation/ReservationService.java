package roomescape.application.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationFilterRequest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.role.RoleRepository;
import roomescape.exception.UnAuthorizedException;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              RoleRepository roleRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request) {
        Member member = memberRepository.getById(request.memberId());
        Theme theme = themeRepository.getById(request.themeId());
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        Reservation reservation = request.toReservation(member, time, theme);
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservation.getDate(), time.getId(), theme.getId())) {
            throw new IllegalArgumentException("이미 존재하는 예약입니다.");
        }
        reservation = reservationRepository.save(reservation);
        reservation.validateCreatedAtAfterReserveTime(reservation.getCreatedAt());
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public List<ReservationResponse> findByFilter(ReservationFilterRequest request) {
        return reservationRepository.findByMemberAndThemeBetweenDates(
                        request.memberId(), request.themeId(), request.startDate(), request.endDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public List<ReservationStatusResponse> findAllByMemberId(long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationStatusResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(long memberId, long id) {
        Reservation reservation = reservationRepository.getById(id);
        if (isAuthorized(memberId, reservation)) {
            reservationRepository.deleteById(reservation.getId());
            return;
        }
        throw new UnAuthorizedException();
    }

    @Transactional
    public void updateMemberById(long id, long memberId, Member member) {
        Reservation reservation = reservationRepository.getById(id);
        if (isAuthorized(memberId, reservation)) {
            reservation.updateMember(member);
            return;
        }
        throw new UnAuthorizedException();
    }

    private boolean isAuthorized(long memberId, Reservation reservation) {
        return roleRepository.isAdminByMemberId(memberId) || reservation.isOwnedBy(memberId);
    }
}
