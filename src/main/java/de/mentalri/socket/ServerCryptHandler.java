package de.mentalri.actionhouse.socket;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ServerCryptHandler extends CryptHandler {
    public ServerCryptHandler(SocketHandler handler) {
        super(handler);
        try {
            KeyPair pair = generateKeyPair();
            PublicKey clientPublicKey = generatePublic(handler.readData());
            byte[] sharedSecret = generateSharedSecret(pair.getPrivate(), clientPublicKey);
            this.secretKey = generateAESKey(sharedSecret);
            this.iv = generateIV();
            handler.sendData(iv);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    private KeyPair generateKeyPair() {
        try { // Generate a Diffie-Hellman key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            // Return the public key to the client
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            handler.sendData(publicKeyBytes);
            return keyPair;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private PublicKey generatePublic(byte[] serverPublicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the server's public key
        KeySpec keySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        return keyFactory.generatePublic(keySpec);
    }
    private byte[] generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            return keyAgreement.generateSecret();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private SecretKeySpec generateAESKey(byte[] sharedSecret) throws Exception {
        // Erzeuge ein Salt
        byte[] salt = generateSalt();

        // Festlege Anzahl der Iterationen und Schlüssellänge
        int iterations = 65536;  // Eine gängige Anzahl von Iterationen
        int keyLength = 256;     // AES-256 verwendet einen 256-Bit-Schlüssel
        handler.sendData(salt);
        // Leite den symmetrischen Schlüssel ab
        return deriveKey(sharedSecret, salt, iterations, keyLength);
    }
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 Bytes = 128 Bit Salt
        random.nextBytes(salt);
        return salt;
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
    public byte[] generateIV() {
        byte[] iv = new byte[16];  // AES block size is 16 bytes (128 bits)
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}
