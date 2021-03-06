package com.hsob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import com.hsob.model.users.User;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jasypt.digest.StandardStringDigester;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

/**
 * @author carlos
 */

public class Utils {
    protected static final Log logger = LogFactory.getLog(Utils.class);
    private static final String CHARS = "qwertyuiopasdfghjklzxcvbnm";
    private static final String NUMBERS = "1234567890";
    private static final String ALGORITHM = "SHA-256";
    private static final int ITERATIONS = 5000;

    @Autowired
    static MongoTemplate hsobdb;


    public static String generateSalt() {
        Random random = new SecureRandom();
        int passwordSize = 10;
        StringBuilder buff = new StringBuilder();
        for (int j = 0; j < (passwordSize / 2); j++) {
            buff.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        for (int j = 0; j < (passwordSize / 2); j++) {
            buff.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return buff.toString().replace("+", "A");
    }

    public static String generateDigest(String password, String salt) {
        StandardStringDigester digester = new StandardStringDigester();
        digester.setAlgorithm(ALGORITHM);
        digester.setIterations(ITERATIONS);
        digester.setSaltGenerator(new StringFixedSaltGenerator(salt));
        return digester.digest(password);
    }

    public static boolean validatePassword(String password, User user){
        try{
            String digest = Utils.generateDigest(password, user.getSalt());

            if (user.getDigest().equals(digest)){
                return true;
            }
        } catch (Exception e){
            logger.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        return false;
    }

    public static String generateAlphaNumericString(int i){

        byte[] bytearray = new byte[256];
        new Random().nextBytes(bytearray);

        String string = new String(bytearray, Charset.forName("UTF-8"));
        StringBuilder stringBuffer = new StringBuilder();
        String alphaNumericString = string.replaceAll("[^A-Z0-9]", "");

        for (int m = 0; m < alphaNumericString.length(); m++) {
            if (Character.isLetter(alphaNumericString.charAt(m)) && (i > 0) || Character.isDigit(alphaNumericString.charAt(m)) && (i > 0)) {
                stringBuffer.append(alphaNumericString.charAt(m));
                i--;
            }
        }

        return stringBuffer.toString();
    }

    public static void imageTo64Base(MultipartFile file, String username) throws IOException {

        File outputFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(outputFile);

        FileInputStream fileInputStreamReader = new FileInputStream(outputFile);
        byte[] bytes = new byte[(int)outputFile.length()];
        fileInputStreamReader.read(bytes);
        String h = Arrays.toString(Base64.encodeBase64(bytes));

        Update update = new Update();
        update.set("photo", h);

        hsobdb.upsert(new Query(Criteria.where("username").is(username)), update, User.class);

    }


}
