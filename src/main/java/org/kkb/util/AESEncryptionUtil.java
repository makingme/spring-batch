package org.kkb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryptionUtil {
    private static final Logger log = LoggerFactory.getLogger(AESEncryptionUtil.class);
    private static final String ALGORITHM = "AES";
    private static final byte[] SECRET_KEY = "MySecretKey12345".getBytes();

    public static String encrypt(String plainText){
        if(plainText == null || plainText.trim().isEmpty()){
            return plainText;
        }
        String encryptedText = plainText;
        try {
            SecretKey key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
        }catch (Exception e){
            log.error("{} 문자열 암호화 중 에러 발생", plainText);
            log.error("에러 상세", e);
        }
        return encryptedText;
    }

    public static String decrypt(String encryptedText) {
        if(encryptedText == null || encryptedText.trim().isEmpty()){
            return encryptedText;
        }
        String decryptedText = encryptedText;
        try {
            SecretKey key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            decryptedText = new String(decryptedBytes);
        }catch (Exception e){
            log.error("{} 문자열 복호화 중 에러 발생", encryptedText);
            log.error("에러 상세", e);
        }
        return decryptedText;
    }

    public static void main(String[] args) {
        if(args == null || args.length < 1){
            System.out.println("첫 번째 파라미터 필수 값 누락(암호화할 평문 암호)");
            System.exit(0);
        }
        String plainPassword = args[0];
        String encryptedPassword = AESEncryptionUtil.encrypt(plainPassword);
        System.out.println("Plain Password: " + plainPassword);
        System.out.println("Encrypted Password: " + encryptedPassword);
    }
}
