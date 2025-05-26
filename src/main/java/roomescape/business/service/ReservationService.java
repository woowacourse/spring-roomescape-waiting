package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.domain.WaitInfo;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.persistence.repository.WaitInfoRepository;
import roomescape.presentation.dto.ReservationMineResponse;
import roomescape.presentation.dto.ReservationResponse;
import roomescape.presentation.dto.WaitInfoResponse;
import roomescape.presentation.dto.WaitResponse;

@Service
public class ReservationService {

    private static final Long FIRST_RANK = 1L;
    private static final Long RANK_START_VALUE = 1L;

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitInfoRepository waitInfoRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final MemberRepository memberRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final WaitInfoRepository waitInfoRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitInfoRepository = waitInfoRepository;
    }

    public ReservationResponse insert(
            final Long memberId,
            final Long themeId,
            final LocalDate date,
            final Long timeId
    ) {
        final Member member = getMemberOrThrow(memberId);
        final ReservationTime reservationTime = getReservationTimeOrThrow(timeId);
        final Theme theme = getThemeOrThrow(themeId);

        validateDateAndTimeIsFuture(date, reservationTime.getStartAt());

        final Reservation reservation = findOrCreateReservation(date, reservationTime, theme);

        validateWaitInfoEmpty(reservation.getId());
        final WaitInfo waitInfo = new WaitInfo(member, reservation, FIRST_RANK);
        waitInfoRepository.save(waitInfo);

        return new ReservationResponse(
                waitInfo.getId(),
                member.getName(),
                theme.getName(),
                date,
                reservationTime.getStartAt()
        );
    }

    public WaitInfoResponse insertWait(
            final Long memberId,
            final Long themeId,
            final LocalDate date,
            final Long timeId
    ) {
        final Member member = getMemberOrThrow(memberId);
        final ReservationTime reservationTime = getReservationTimeOrThrow(timeId);
        final Theme theme = getThemeOrThrow(themeId);

        validateDateAndTimeIsFuture(date, reservationTime.getStartAt());

        final Reservation reservation = findOrCreateReservation(date, reservationTime, theme);

        validateWaitInfoExistsByReservationId(reservation.getId());
        validateWaitInfoIsNotDuplicate(memberId, reservation.getId());

        final WaitInfo waitInfo = new WaitInfo(member, reservation, getNextRank(reservation.getId()));
        waitInfoRepository.save(waitInfo);
        return new WaitInfoResponse(
                waitInfo.getId(),
                waitInfo.getMember().getId(),
                waitInfo.getMember().getName(),
                waitInfo.getRank()
        );
    }

    private Member getMemberOrThrow(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당하는 사용자를 찾을 수 없습니다. 사용자 id: %d".formatted(memberId)));
    }

    private ReservationTime getReservationTimeOrThrow(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("해당하는 방탈출 예약 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(timeId)));
    }

    private Theme getThemeOrThrow(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(themeId)));
    }

    private Reservation findOrCreateReservation(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        final Long timeId = reservationTime.getId();
        final Long themeId = theme.getId();
        if (!reservationRepository.existsByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)) {
            final Reservation reservation = new Reservation(date, reservationTime, theme);
            reservationRepository.save(reservation);
        }
        return reservationRepository.findByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new NotFoundException("예약 정보를 찾을 수 없습니다."));
    }

    private Long getNextRank(final Long reservationId) {
        return waitInfoRepository.countByReservationId(reservationId) + RANK_START_VALUE;
    }

    private void validateDateAndTimeIsFuture(final LocalDate date, final LocalTime time) {
        final LocalDateTime now = LocalDateTime.now();

        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        if (reservationDateTime.isBefore(now)) {
            throw new InvalidDateAndTimeException("방탈출 예약 날짜와 시간이 현재보다 과거일 수 없습니다.");
        }
    }

    private void validateWaitInfoEmpty(Long reservationId) {
        if (waitInfoRepository.existsByReservationId(reservationId)) {
            throw new DuplicateException("예약이 이미 존재합니다. 예약대기를 사용해주세요.");
        }
    }

    private void validateWaitInfoExistsByReservationId(Long reservationId) {
        if (!waitInfoRepository.existsByReservationId(reservationId)) {
            throw new NotFoundException("예약 대기자가 없습니다. 예약하기를 사용해주세요.");
        }
    }

    private void validateWaitInfoIsNotDuplicate(final Long memberId, final Long reservationId) {
        if (waitInfoRepository.existsByMemberIdAndReservationId(memberId, reservationId)) {
            throw new DuplicateException("추가하려는 예약 대기가 이미 존재합니다. memberId: %d, reservationId: %d"
                    .formatted(memberId, reservationId));
        }
    }

    public List<ReservationResponse> findAll() {
        final List<WaitInfo> waitInfos = waitInfoRepository.findByRank(1L);
        return waitInfos.stream()
                .map(waitInfo -> new ReservationResponse(
                        waitInfo.getId(),
                        waitInfo.getMember().getName(),
                        waitInfo.getReservation().getTheme().getName(),
                        waitInfo.getReservation().getDate(),
                        waitInfo.getReservation().getReservationTime().getStartAt()
                ))
                .toList();
    }

    public List<ReservationResponse> findAllFilter(
            final Long memberId,
            final Long themeId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        final List<WaitInfo> waitInfos = waitInfoRepository.filterByMemberIdAndThemeIdAndStartDateAndEndDateAndRank(
                memberId,
                themeId,
                startDate,
                endDate,
                1L
        );

        return waitInfos.stream()
                .map(waitInfo -> new ReservationResponse(
                        waitInfo.getId(),
                        waitInfo.getMember().getName(),
                        waitInfo.getReservation().getTheme().getName(),
                        waitInfo.getReservation().getDate(),
                        waitInfo.getReservation().getReservationTime().getStartAt()
                ))
                .toList();
    }

    public void deleteById(final Long waitInfoId) {
        validateWaitInfoExistsById(waitInfoId);
        final WaitInfo waitInfo = waitInfoRepository.findById(waitInfoId).get();
        waitInfoRepository.deleteById(waitInfoId);

        final Long reservationId = waitInfo.getReservation().getId();
        updateWaitInfosRankAfterDelete(reservationId);
    }

    private void validateWaitInfoExistsById(final Long waitInfoId) {
        if (!waitInfoRepository.existsById(waitInfoId)) {
            throw new NotFoundException("해당하는 예약 대기를 찾을 수 없습니다. 예약 대기 id: %d".formatted(waitInfoId));
        }
    }

    private void updateWaitInfosRankAfterDelete(final Long reservationId) {
        final List<WaitInfo> waitInfos = getSortedWaitInfosByCreatedAt(reservationId);
        reassignRanks(waitInfos);
    }

    private List<WaitInfo> getSortedWaitInfosByCreatedAt(final Long reservationId) {
        return waitInfoRepository.findByReservationId(reservationId).stream()
                .sorted((w1, w2) -> w1.getCreatedAt().compareTo(w2.getCreatedAt()))
                .toList();
    }

    private void reassignRanks(final List<WaitInfo> waitInfos) {
        for (int i = 0; i < waitInfos.size(); i++) {
            final WaitInfo waitInfo = waitInfos.get(i);
            final WaitInfo updatedWaitInfo = new WaitInfo(
                    waitInfo.getId(),
                    waitInfo.getMember(),
                    waitInfo.getReservation(),
                    calculateRank(i)
            );
            waitInfoRepository.save(updatedWaitInfo);
        }
    }

    private Long calculateRank(int index) {
        return (long) index + RANK_START_VALUE;
    }

    public void deleteWaitInfoByIdAndMemberId(final Long waitInfoId, final Long memberId) {
        validateWaitInfoExistsByIdAndMemberId(waitInfoId, memberId);

        deleteById(waitInfoId);
    }

    private void validateWaitInfoExistsByIdAndMemberId(final Long waitInfoId, final Long memberId) {
        if (!waitInfoRepository.existsByIdAndMemberId(waitInfoId, memberId)) {
            throw new NotFoundException(
                    "해당하는 예약 대기를 찾을 수 없습니다. 예약 대기 id: %d, 멤버 id: %d".formatted(waitInfoId, memberId));
        }
    }

    public List<ReservationMineResponse> findByMemberId(final Long memberId) {
        final List<WaitInfo> memberWaitInfos = waitInfoRepository.findByMemberId(memberId);
        return memberWaitInfos.stream()
                .map(waitInfo -> new ReservationMineResponse(
                        waitInfo.getReservation().getId(),
                        waitInfo.getReservation().getTheme().getName(),
                        waitInfo.getReservation().getDate(),
                        waitInfo.getReservation().getReservationTime().getStartAt(),
                        calculateStatus(waitInfo),
                        waitInfo.getId()))
                .toList();
    }

    private String calculateStatus(final WaitInfo waitInfo) {
        final Long rank = waitInfoRepository.countByIdLessThanEqualAndReservationId(
                waitInfo.getId(),
                waitInfo.getReservation().getId());

        if (rank == 1) {
            return "예약";
        }
        return "%d번째 예약대기".formatted(rank);
    }

    public List<WaitResponse> findWaitInfoByStatusNotApprove() {
        final List<WaitInfo> waitInfos = waitInfoRepository.findByRankNot(1L);

        return waitInfos.stream()
                .map(waitInfo -> new WaitResponse(
                        waitInfo.getId(),
                        waitInfo.getMember().getName(),
                        waitInfo.getReservation().getTheme().getName(),
                        waitInfo.getReservation().getDate(),
                        waitInfo.getReservation().getReservationTime().getStartAt()))
                .toList();
    }
}
