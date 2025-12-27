package com.smartspend.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageUtils {
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB   

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    public boolean isImageSizeValid(long imageSize) {
        return imageSize <= MAX_IMAGE_SIZE_BYTES;
    }

    public boolean isImageTypeValid(String imageType) {
        return ALLOWED_IMAGE_TYPES.contains(imageType.toLowerCase());
    }

    public boolean isValidImage(MultipartFile imageFile) {

        if (imageFile.getContentType() == null) {
            return false;
        }

        return isImageTypeValid(imageFile.getContentType()) && isImageSizeValid(imageFile.getSize());
    }


    public byte[] processImage(MultipartFile imageFile) throws IOException {
        if (!isValidImage(imageFile)) {
            return null;
        }
        return imageFile.getBytes();
    }
}
