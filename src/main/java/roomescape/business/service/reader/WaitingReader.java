package roomescape.business.service.reader;

import roomescape.business.dto.WaitingDto;

import java.util.List;

public interface WaitingReader {

    /**
     * 대기 중인 예약들을 모두 반환합니다.
     *
     * @return 대기중인 예약 응답들
     */
    List<WaitingDto> getAll();
}
