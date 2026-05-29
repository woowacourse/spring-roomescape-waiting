package roomescape.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import roomescape.service.FileUploader;

@Component
public class LocalFileUploader implements FileUploader {
    private static final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/images/";

    @Override
    public String upload(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            file.transferTo(new File(filePath));

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장에 실패했습니다.", e);
        }

        return "/images/" + fileName;
    }
}
