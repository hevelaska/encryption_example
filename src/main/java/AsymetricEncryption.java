import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AsymetricEncryption {

    public static void main(String[] args) {

        var encryptionTool = new EncryptionTool();

        try {
            var privateKey = encryptionTool.readPrivateKey();
            var publicKey = encryptionTool.readPublicKey();

            System.out.println("Keys loaded!");

            String secret = "My password is password";

            System.out.println("Secret phrase: " + secret);

            String encodedSecret = encryptionTool.encrypt(publicKey, secret);

            System.out.println("Encoded secret phrase: " + encodedSecret);

            String decryptedSecret = encryptionTool.decrypt(privateKey, encodedSecret);

            System.out.println("Decoded secret phrase: " + decryptedSecret);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading keys");
        }

    }

    private static class EncryptionTool {
        public PrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
            InputStream is = getClass().getClassLoader().getResourceAsStream("privateKey.der");
            assert is != null;
            byte[] keyBytes = is.readAllBytes();
            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }

        public PublicKey readPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
            InputStream is = getClass().getClassLoader().getResourceAsStream("publicKey.der");
            assert is != null;
            byte[] keyBytes = is.readAllBytes();
            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }

        public String encrypt(PublicKey publicKey, String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            var encodedBytes = cipher.doFinal(data.getBytes());
            return new String(Base64.getEncoder().encode(encodedBytes));
        }

        public String decrypt(PrivateKey privateKey, String encodedBase64Data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            byte[] encryptedData = Base64.getDecoder().decode(encodedBase64Data);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            var decodedBytes = cipher.doFinal(encryptedData);
            return new String(decodedBytes);
        }

    }

}
