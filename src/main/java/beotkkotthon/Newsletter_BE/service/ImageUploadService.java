package beotkkotthon.Newsletter_BE.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageUploadService {
    String uploadImage(MultipartFile file) throws IOException;
}
