package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationSlotRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AdminCreateReservationRequest;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.MemberCreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByfilterRequest;
import roomescape.reservation.ui.dto.response.AdminReservationResponse;
import roomescape.reservation.ui.dto.response.AdminReservationWaitingResponse;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.MemberReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public MemberReservationResponse createForMember(
            final MemberCreateReservationRequest request,
            final Long memberId
    ) {
        final Reservation reservation = registerReservation(request.date(), request.timeId(), request.themeId(),
                memberId);

        reservationRepository.flush();

        return MemberReservationResponse.from(reservation,
                reservationRepository.getReservationRankById(reservation.getId()));
    }

    @Transactional
    public AdminReservationResponse createForAdmin(final AdminCreateReservationRequest request) {
        final Reservation reservation = registerReservation(request.date(), request.timeId(), request.themeId(),
                request.memberId());

        reservationRepository.flush();

        return AdminReservationResponse.from(reservation,
                reservationRepository.getReservationRankById(reservation.getId()));
    }

    @Transactional
    public void deleteReservation(final Long reservationId, final MemberAuthInfo memberAuthInfo) {
        final Reservation reservation = reservationRepository.getByIdOrThrow(reservationId);
        final Member member = memberRepository.getByIdOrThrow(memberAuthInfo.id());
        member.validateDeletableReservation(reservation);

        final ReservationSlot reservationSlot = reservation.getReservationSlot();
        reservation.delete();
        reservationRepository.delete(reservation);

        resolveSlotAfterChange(reservationSlot);
    }

    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAll() {
        return reservationSlotRepository.findAll()
                .stream()
                .map(this::mapSlotReservations)
                .flatMap(List::stream)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAllByFilter(final ReservationsByfilterRequest request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                        request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
                )
                .stream()
                .map(reservation ->
                        AdminReservationResponse.from(
                                reservation,
                                reservationRepository.getReservationRankById(reservation.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeRequest request
    ) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<LocalTime> bookedTimes = reservationSlotRepository.findAllByDateAndThemeId(
                        request.date(),
                        request.themeId()
                ).stream()
                .map(reservationSlot -> reservationSlot.getTime().getStartAt())
                .toList();

        return reservationTimes.stream()
                .map(reservationTime ->
                        new AvailableReservationTimeResponse(
                                reservationTime.getId(),
                                reservationTime.getStartAt(),
                                bookedTimes.contains(reservationTime.getStartAt())
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findReservationsByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(reservation -> MemberReservationResponse.from(reservation,
                        reservationRepository.getReservationRankById(reservation.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminReservationWaitingResponse> findReservationWaitings() {
        return reservationSlotRepository.findAll().stream()
                .map(ReservationSlot::getWaitingReservations)
                .flatMap(List::stream)
                .map(AdminReservationWaitingResponse::from)
                .toList();
    }

    private Reservation registerReservation(final LocalDate date, final Long timeId, final Long themeId,
                                            final Long memberId) {

        final ReservationSlot reservationSlot = getReservationSlot(date, timeId, themeId);
        validateMemberDuplicateReservation(reservationSlot, memberId);

        final Member member = memberRepository.getByIdOrThrow(memberId);
        final Reservation reservation = new Reservation(member, reservationSlot);
        reservationSlot.addReservation(reservation);
        resolveSlotAfterChange(reservationSlot);
        return reservation;
    }

    private ReservationSlot getReservationSlot(final LocalDate date, final Long timeId, final Long themeId) {
        return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseGet(() -> registerReservationSlot(date, timeId, themeId));
    }

    private ReservationSlot registerReservationSlot(final LocalDate date, final Long timeId, final Long themeId) {
        final ReservationTime time = reservationTimeRepository.getByIdOrThrow(timeId);
        final Theme theme = themeRepository.getByIdOrThrow(themeId);

        return reservationSlotRepository.save(new ReservationSlot(date, time, theme));
    }

    private void resolveSlotAfterChange(final ReservationSlot reservationSlot) {
        if (reservationSlot.shouldBeDeleted()) {
            reservationSlotRepository.delete(reservationSlot);
            return;
        }
        reservationSlot.assignConfirmedIfEmpty();
    }

    private List<AdminReservationResponse> mapSlotReservations(final ReservationSlot reservationSlot) {
        return reservationSlot.getAllReservations().stream()
                .map(reservation -> AdminReservationResponse.from(
                        reservation,
                        reservationRepository.getReservationRankById(reservation.getId())))
                .toList();
    }

    private void validateMemberDuplicateReservation(final ReservationSlot reservationSlot, final Long memberId) {
        if (reservationRepository.existsByReservationSlotAndMemberId(reservationSlot, memberId)) {
            throw new AlreadyExistException("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
        }
    }
}
