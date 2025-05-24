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

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitInfoRepository waitInfoRepository;

    public ReservationService(final ReservationRepository reservationRepository,
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

    public ReservationResponse insert(final LocalDate date, final Long memberId, final Long timeId,
                                      final Long themeId
    ) {
        validateMemberIdExists(memberId);
        final Member member = memberRepository.findById(memberId).get();
        validateTimeIdExists(timeId);
        final ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();
        validateThemeIdExists(themeId);
        final Theme theme = themeRepository.findById(themeId).get();

        validateIsDuplicate(date, timeId, themeId);
        validateDateAndTimeIsFuture(date, reservationTime.getStartAt());
        // TODO: 검증 변경하기, "예약이 존재합니다. 예약대기를 사용해주세요."

        final Reservation reservation = new Reservation(date, member, reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);

        final WaitInfo waitInfo = new WaitInfo(member, reservation);
        waitInfoRepository.save(waitInfo);
        return ReservationResponse.from(savedReservation);
    }

    // TODO: 테스트 추가
    public WaitInfoResponse insertWait(final LocalDate date, final Long memberId, final Long timeId,
                                       final Long themeId) {
        validateMemberIdExists(memberId);
        final Member member = memberRepository.findById(memberId).get();
        validateTimeIdExists(timeId);
        final ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();
        validateThemeIdExists(themeId);

        validateDateAndTimeIsFuture(date, reservationTime.getStartAt());

        validateReservationExists(date, timeId, themeId);
        final Reservation reservation = reservationRepository.findByDateAndReservationTimeIdAndThemeId(
                date, timeId, themeId).get();
        validateWaitInfoExists(reservation.getId());
        validateWaitInfoIsNotDuplicate(memberId, reservation.getId());

        final WaitInfo waitInfo = new WaitInfo(member, reservation);
        waitInfoRepository.save(waitInfo);
        return WaitInfoResponse.from(waitInfo);
    }

    private void validateMemberIdExists(final Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException("해당하는 사용자를 찾을 수 없습니다. 사용자 id: %d".formatted(memberId));
        }
    }

    private void validateTimeIdExists(final Long timeId) {
        if (!reservationTimeRepository.existsById(timeId)) {
            throw new NotFoundException("해당하는 방탈출 예약 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(timeId));
        }
    }

    private void validateThemeIdExists(final Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(themeId));
        }
    }

    private void validateIsDuplicate(final LocalDate date, final Long playTimeId, final Long themeId) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeId(date, playTimeId, themeId)) {
            throw new DuplicateException("추가 하려는 예약과 같은 날짜, 시간, 테마의 예약이 이미 존재합니다.");
        }
    }

    private void validateDateAndTimeIsFuture(final LocalDate date, final LocalTime time) {
        final LocalDateTime now = LocalDateTime.now();

        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        if (reservationDateTime.isBefore(now)) {
            throw new InvalidDateAndTimeException("방탈출 예약 날짜와 시간이 현재보다 과거일 수 없습니다.");
        }
    }

    // TODO: 테스트 추가
    private void validateReservationExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (!reservationRepository.existsByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)) {
            throw new NotFoundException("해당하는 방탈출 예약을 찾을 수 없습니다. 방탈출 id: %d".formatted(themeId));
        }
    }

    // TODO: 테스트 추가
    private void validateWaitInfoExists(Long reservationId) {
        if (!waitInfoRepository.existsByReservationId(reservationId)) {
            throw new NotFoundException("예약 대기자가 없습니다. 예약하기를 사용해주세요.");
        }
    }

    // TODO: 테스트 추가
    private void validateWaitInfoIsNotDuplicate(final Long memberId, final Long reservationId) {
        if (waitInfoRepository.existsByMemberIdAndReservationId(memberId, reservationId)) {
            throw new DuplicateException("추가하려는 예약 대기가 이미 존재합니다. memberId: %d, reservationId: %d"
                    .formatted(memberId, reservationId));
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllFilter(final Long memberId, final Long themeId, final LocalDate startDate,
                                                   final LocalDate endDate) {
        return reservationRepository.findAllByFilter(memberId, themeId, startDate, endDate)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("해당하는 방탈출 예약을 찾을 수 없습니다. 방탈출 id: %d".formatted(id));
        }
        reservationRepository.deleteById(id);
    }

    public List<ReservationMineResponse> findByMemberId(final Long memberId) {
        final List<WaitInfo> memberWaitInfos = waitInfoRepository.findByMemberId(memberId);
        return memberWaitInfos.stream()
                .map(waitInfo -> new ReservationMineResponse(waitInfo.getReservation().getId(),
                        waitInfo.getReservation().getTheme().getName(),
                        waitInfo.getReservation().getDate(),
                        waitInfo.getReservation().getReservationTime().getStartAt(),
                        calculateStatus(waitInfo),
                        waitInfo.getId()))
                .toList();
    }

    // TODO: 테스트 추가, 랭크 계산이 잘 계산되는지 확인하기
    private String calculateStatus(final WaitInfo waitInfo) {
        final Long rank = waitInfoRepository.countByIdLessThanEqualAndReservationId(
                waitInfo.getId(),
                waitInfo.getReservation().getId());

        if(rank == 1) {
            return "예약";
        }
        return "%d번째 예약대기".formatted(rank);
    }

    // TODO: 테스트 추가, 예약 대기 삭제가 잘 되는지 확인
    public void deleteWaitInfoByIdAndMemberId(final Long waitInfoId, final Long memberId) {
        if (!waitInfoRepository.existsByIdAndMemberId(waitInfoId, memberId)) {
            throw new NotFoundException("해당하는 예약 대기를 찾을 수 없습니다. 예약 대기 id: %d, 멤버 id: %d".formatted(waitInfoId, memberId));
        }

        waitInfoRepository.deleteById(waitInfoId);
    }

    // TODO: 테스트 추가. 승인되지 않은 예약만 가져오는지 확인
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
