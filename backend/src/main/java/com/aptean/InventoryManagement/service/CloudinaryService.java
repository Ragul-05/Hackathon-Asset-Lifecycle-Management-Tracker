package com.aptean.InventoryManagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryService(Cloudinary cloudinary, @Value("${cloudinary.folder}") String folder) {
        this.cloudinary = cloudinary;
        this.folder = folder;
    }

    public Map upload(MultipartFile file, String resourceType) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", folder
        ));
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }
}
