package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.AuthorizationException;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.service.dto.MemberReservationCreate;
import roomescape.reservation.service.dto.MyReservationInfo;
import roomescape.reservation.service.dto.WaitingCreate;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              MemberReservationRepository memberReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        return memberReservationRepository.findBy(
                        request.getMemberId(),
                        request.getThemeId(),
                        ReservationStatus.APPROVED,
                        request.getStartDate(),
                        request.getEndDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationInfo> findMyReservations(AuthInfo authInfo) {
        Member member = getMember(authInfo.getId());
        return memberReservationRepository.findByMember(member.getId())
                .stream()
                .map(MyReservationInfo::of)
                .toList();
    }

    @Transactional
    public ReservationResponse createMemberReservation(MemberReservationCreate memberReservationCreate) {
        ReservationTime reservationTime = getReservationTime(memberReservationCreate.timeId());
        Theme theme = getTheme(memberReservationCreate.themeId());
        Member member = getMember(memberReservationCreate.memberId());
        Reservation reservation = getReservation(memberReservationCreate.date(), reservationTime, theme);

        validatePastReservation(reservation);
        validateDuplicatedReservation(reservation, member);

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.APPROVED));
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    private void validateDuplicatedReservation(Reservation reservation, Member member) {
        if (memberReservationRepository.existsByReservationAndMember(reservation, member)) {
            throw new BadRequestException(ErrorType.DUPLICATED_RESERVATION_ERROR);
        }
    }

    @Transactional
    public void deleteMemberReservation(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = getMemberReservation(memberReservationId);
        Member member = getMember(authInfo.getId());
        delete(member, memberReservation);
    }

    @Transactional
    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservationId(reservationId);
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationResponse> getWaiting() {
        return memberReservationRepository.findAllByReservationStatus(ReservationStatus.PENDING)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse addWaiting(WaitingCreate waitingCreate) {
        ReservationTime reservationTime = getReservationTime(waitingCreate.timeId());
        Theme theme = getTheme(waitingCreate.themeId());
        Member member = getMember(waitingCreate.memberId());
        Reservation reservation = getReservation(waitingCreate.date(), reservationTime, theme);

        validatePastReservation(reservation);
        validateDuplicatedReservation(reservation, member);

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.PENDING));

        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    @Transactional
    public void deleteWaiting(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = getMemberReservation(memberReservationId);
        Member member = getMember(authInfo.getId());
        validateWaitingReservation(memberReservation);
        delete(member, memberReservation);
    }

    @Transactional
    public void approveWaiting(AuthInfo authInfo, long memberReservationId) {
        Member member = getMember(authInfo.getId());
        MemberReservation memberReservation = getMemberReservation(memberReservationId);

        validateAdminPermission(member);
        validateWaitingReservation(memberReservation);
        memberReservation.approve();
    }

    @Transactional
    public void denyWaiting(AuthInfo authInfo, long memberReservationId) {
        Member member = getMember(authInfo.getId());
        MemberReservation memberReservation = getMemberReservation(memberReservationId);

        validateAdminPermission(member);
        validateWaitingReservation(memberReservation);
        memberReservation.deny();
    }

    private static void validateAdminPermission(Member member) {
        if (!member.isAdmin()) {
            throw new AuthorizationException(ErrorType.NOT_ALLOWED_PERMISSION_ERROR);
        }
    }

    private void delete(Member member, MemberReservation memberReservation) {
        if (!canDelete(member, memberReservation)) {
            throw new AuthorizationException(ErrorType.NOT_A_RESERVATION_MEMBER);
        }
        memberReservationRepository.deleteById(memberReservation.getId());
    }

    private boolean canDelete(Member member, MemberReservation memberReservation) {
        return member.isAdmin() || memberReservation.isRegisteredMember(member);
    }

    private void validatePastReservation(Reservation reservation) {
        if (reservation.isPast()) {
            throw new BadRequestException(ErrorType.INVALID_REQUEST_ERROR);
        }
    }

    private void validateWaitingReservation(MemberReservation memberReservation) {
        if (memberReservationRepository.isFirstReservation(memberReservation.getId())) {
            throw new BadRequestException(ErrorType.NOT_A_WAITING_RESERVATION);
        }
    }

    private ReservationTime getReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_TIME_NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException(ErrorType.THEME_NOT_FOUND));
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND));
    }

    private Reservation getReservation(LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.findReservationByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> reservationRepository.save(new Reservation(date, time, theme)));
    }

    private MemberReservation getMemberReservation(long memberReservationId) {
        return memberReservationRepository.findById(memberReservationId)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_RESERVATION_NOT_FOUND));
    }
}
