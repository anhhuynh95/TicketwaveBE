package nl.fontys.s3.ticketwave_s3.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import nl.fontys.s3.ticketwave_s3.Configuration.CloudinaryConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(CloudinaryConfig config) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", config.getCloudName(),
                "api_key", config.getApiKey(),
                "api_secret", config.getApiSecret()));
    }

    public String uploadEventImage(File file, String eventId) throws IOException {
        var uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("public_id", "events/" + eventId));
        return uploadResult.get("url").toString();
    }

    public String generateImageUrl(String eventId) {
        return cloudinary.url()
                .publicId("events/" + eventId) // Use the same structure as used during upload
                .format("png") // Specify the image format
                .generate();
    }
    /** Create a dedicated, secure temporary directory */
    public Path createSecureTempDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("secure-temp-dir-");
        File dirFile = tempDir.toFile();

        // Skip setting permissions in test environments
        if (isTestEnvironment()) {
            return tempDir;
        }

        if (!dirFile.setReadable(true, true) || !dirFile.setWritable(true, true) || !dirFile.setExecutable(false, true)) {
            throw new IOException("Failed to set secure permissions on temporary directory.");
        }
        return tempDir;
    }

    /** Check if running in a test environment */
    private boolean isTestEnvironment() {
        return System.getProperty("java.class.path").contains("test");
    }
}

