package Common;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class RSAKeys {
    private Cipher cipher;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAKeys() {
        privateKey = null;
        publicKey = null;
    }

    public void generate() {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public static String encrypt(String msg, PublicKey key) {
        if (key == null) {
            return msg;
        }

        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] enc = cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
            return new String(enc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static String decrypt(String msg, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] dec = msg.getBytes();
            return new String(cipher.doFinal(dec), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static byte[] encrypt(byte[] input, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return input;
    }

    public static byte[] decrypt(byte[] input, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return input;
    }
}
