package roomescape.business.application_service.reader;

import roomescape.business.dto.ThemeDto;

import java.util.List;

public interface ThemeReader {

    List<ThemeDto> getAll();

    List<ThemeDto> getPopulars(int size);
}
