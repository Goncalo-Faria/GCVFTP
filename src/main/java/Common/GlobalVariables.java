package Common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class GlobalVariables {
    public static String filesPath = "/home/machadovilaca/Desktop/files/";

    private static String publicKeyFile = "/tmp/public";
    private static PublicKey publicKey = null;
    private static String privateKeyFile = "/tmp/private";
    private static PrivateKey privateKey = null;

    public static void main(String[] args) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kpair = kpg.genKeyPair();

            byte[] publicKeyBytes = kpair.getPublic().getEncoded();
            FileOutputStream fos = new FileOutputStream(publicKeyFile);
            fos.write(publicKeyBytes);
            fos.close();

            byte[] privateKeyBytes = kpair.getPrivate().getEncoded();
            fos = new FileOutputStream(privateKeyFile);
            fos.write(privateKeyBytes);
            fos.close();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    public static PublicKey getPublicKey() {
        if(publicKey != null) {
            return publicKey;
        }

        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(publicKeyFile));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchFileException e) {
            main(null);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKey() {
        if(privateKey != null) {
            return privateKey;
        }

        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchFileException e) {
            main(null);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
