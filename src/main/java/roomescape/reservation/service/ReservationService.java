package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationMemberCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public Reservation save(
            final ReservationMemberCreateRequest request,
            final LoginMemberInToken loginMember
    ) {
        Reservation reservation = getValidatedReservation(
                request.date(), request.timeId(), request.themeId(), loginMember.id());

        validateDuplicateReservation(reservation);
        validateDateTime(reservation.getDate(), reservation.getTime().getStartAt());

        return reservationRepository.save(reservation);
    }

    public Reservation saveByAdmin(final ReservationAdminCreateRequest request) {
        Reservation reservation = getValidatedReservation(
                request.date(), request.timeId(), request.themeId(), request.memberId());

        validateDuplicateReservation(reservation);
        validateDateTime(reservation.getDate(), reservation.getTime().getStartAt());

        return reservationRepository.save(reservation);
    }

    private void validateDateTime(LocalDate date, LocalTime time) {
        final LocalDate today = LocalDate.now();
        final LocalTime now = LocalTime.now();

        if (date.isBefore(today)) {
            throw new IllegalArgumentException("지난 날짜는 예약할 수 없습니다.");
        }

        if (date.isEqual(today) && time.isBefore(now)) {
            throw new IllegalArgumentException("지난 시간은 예약할 수 없습니다.");
        }
    }

    private Reservation getValidatedReservation(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return new Reservation(member, date, theme, reservationTime);
    }

    private void validateDuplicateReservation(final Reservation reservation) {
        if (reservationRepository.existsByDateAndReservationTimeStartAt(reservation.getDate(), reservation.getTime()
                .getStartAt())) {
            throw new IllegalArgumentException("중복된 예약이 있습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllBySearch(final ReservationSearchRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        return reservationRepository.findAllByMemberAndThemeAndDateBetween(
                        member,
                        theme,
                        request.dateFrom(),
                        request.dateTo()
                ).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(MyReservationResponse::new)
                .toList();
    }

    @Transactional
    public void delete(final Long id, final LoginMemberInToken loginMember) {
        final Reservation target = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 입니다."));

        final Role role = loginMember.role();
        if (!role.isAdmin() && !target.isSameMemberId(loginMember.id())) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        waitingRepository.findFirstByThemeIdAndDateAndReservationTimeStartAt(
                target.getTheme().getId(),
                target.getDate(),
                target.getTime().getStartAt()
        ).ifPresent(firstWaiting -> {
            reservationRepository.save(firstWaiting.toReservation());
            waitingRepository.delete(firstWaiting);
        });

        reservationRepository.delete(target);
    }
}
