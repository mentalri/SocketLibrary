package de.mentalri.actionhouse.socket;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ClientCryptHandler extends CryptHandler{
    public ClientCryptHandler(SocketHandler handler) {
        super(handler);
        try {
            byte[] serverPublicKeyBytes = handler.readData();
            byte[] sharedSecret = generateSharedSecret(serverPublicKeyBytes);
            byte[] salt = handler.readData();
            int iterations = 65536;  // Eine gängige Anzahl von Iterationen
            int keyLength = 256;     // AES-256 verwendet einen 256-Bit-Schlüssel
            secretKey = deriveKey(sharedSecret,salt,iterations,keyLength);
            this.iv = handler.readData();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public PublicKey generatePublic(byte[] serverPublicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the server's public key
        KeySpec keySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        return keyFactory.generatePublic(keySpec);
    }
    public byte[] generateSharedSecret(byte[] serverPublicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IOException {
        PublicKey serverPublicKey = generatePublic(serverPublicKeyBytes);
        // Generate a Diffie-Hellman key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //Send server the public key
        byte[] clientPublicKeyBytes = keyPair.getPublic().getEncoded();
        handler.sendData(clientPublicKeyBytes);
        // Calculate the shared secret using the client's private key and the server's public key
        KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(serverPublicKey, true);
        return keyAgreement.generateSecret();
    }
    private SecretKeySpec deriveKey(byte[] sharedSecret, byte[] salt, int iterations, int keyLength) throws Exception {
        // Konfiguriere PBKDF2 mit HMAC-SHA256
        PBEKeySpec spec = new PBEKeySpec(
                toHex(sharedSecret).toCharArray(), // Shared Secret als Passwort
                salt,                             // Salt
                iterations,                       // Anzahl der Iterationen
                keyLength                         // Schlüssellänge in Bits (z.B. 256)
        );

        // Verwende PBKDF2 mit SHA-256
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(spec).getEncoded();

        // Gib den abgeleiteten Schlüssel zurück (AES-Schlüssel in diesem Beispiel)
        return new SecretKeySpec(key, "AES");
    }
    public String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
