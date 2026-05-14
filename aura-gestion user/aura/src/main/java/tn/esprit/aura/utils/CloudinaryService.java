package tn.esprit.aura.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {

    private static final String CLOUD_NAME = "dofap5wt0";
    private static final String API_KEY = "213831313941832";
    private static final String API_SECRET = "q8PU3_70fnA2EB_LrQ-_1TDdYOU";

    private static Cloudinary cloudinary;

    public static synchronized Cloudinary getInstance() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", CLOUD_NAME,
                    "api_key", API_KEY,
                    "api_secret", API_SECRET,
                    "secure", true
            ));
        }
        return cloudinary;
    }

    public static String uploadImage(File file) {
        try {
            Map uploadResult = getInstance().uploader().upload(file, ObjectUtils.emptyMap());
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
