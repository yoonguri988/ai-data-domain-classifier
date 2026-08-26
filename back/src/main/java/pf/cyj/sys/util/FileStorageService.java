package pf.cyj.sys.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

 
@Service  
public class FileStorageService {
   
    private final Path root = Paths.get("uploads");  // 프젝 실행 위치를 기준으로 uploads폴더 생성

    public String upload(MultipartFile file) {
        try {
            if (!Files.exists(root)) {  // 디렉토리 생성확인
                Files.createDirectories(root);  // 중간 경로까지 모두생성
            }
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();  // 파일명 충돌방지
            Path target = root.resolve(filename);   // uploads 디렉토리 안에서 filename 붙여서 최종경로
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);   // 파일 올리기
            return "uploads/" + filename;   
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
}