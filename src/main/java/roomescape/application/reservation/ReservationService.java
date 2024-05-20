package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
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
import roomescape.domain.reservation.ReservationSpec;
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
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              RoleRepository roleRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation create(ReservationRequest request) {
        Member member = memberRepository.getById(request.memberId());
        Theme theme = themeRepository.getById(request.themeId());
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        Reservation reservation = request.toReservation(member, time, theme, LocalDateTime.now(clock));
        return reservationRepository.save(reservation);
    }

    public List<ReservationResponse> findByFilter(ReservationFilterRequest request) {
        Specification<Reservation> specification = ReservationSpec.where()
                .equalsMemberId(request.memberId())
                .equalsThemeId(request.themeId())
                .greaterThanOrEqualsStartDate(request.startDate())
                .lessThanOrEqualsEndDate(request.endDate())
                .build();

        return reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationStatusResponse> findAllByMemberId(long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationStatusResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(long memberId, long id) {
        Reservation reservation = reservationRepository.getById(id);
        if (roleRepository.isAdminByMemberId(memberId) || reservation.isOwnedBy(memberId)) {
            reservationRepository.deleteById(reservation.getId());
            return;
        }
        throw new UnAuthorizedException();
    }
}
