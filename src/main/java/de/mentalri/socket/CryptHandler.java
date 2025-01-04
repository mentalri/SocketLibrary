package de.mentalri.socket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;

public abstract class CryptHandler extends SocketHandler{
    protected SecretKey secretKey;;
    protected byte[] iv;
    public CryptHandler(SocketHandler handler, SecretKey secretKey, byte[] iv) {
        super(handler);
        this.secretKey = secretKey;
        this.iv = iv;
    }

    public CryptHandler(SocketHandler handler) {
        super(handler);
    }

    public byte[] encrypt(byte[] plaintext){
        try {// Create AES Cipher instance (AES/CBC/PKCS5Padding)
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Initialize Cipher for encryption with key and IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Encrypt the plaintext
            return cipher.doFinal(plaintext);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public byte[] decrypt(byte[] ciphertext){
        try { // Create AES Cipher instance (AES/CBC/PKCS5Padding)
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Initialize Cipher for decryption with key and IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // Decrypt the ciphertext
            return cipher.doFinal(ciphertext);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendData(byte[] dataBytes) throws IOException {
        super.sendData(encrypt(dataBytes));
    }

    @Override
    public byte[] readData() throws IOException {
        return decrypt(super.readData());
    }
}
