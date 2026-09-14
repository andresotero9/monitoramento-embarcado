package br.com.monitoramento.camera.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra e decifra a senha das câmeras utilizando AES/GCM.
 *
 * <p>Diferente do hash BCrypt usado para senhas de usuário, a senha da câmera
 * precisa ser recuperável em texto puro para autenticar a conexão RTSP, por isso
 * usamos criptografia simétrica reversível em vez de hashing.</p>
 *
 * <p>A chave é derivada de {@code app.camera.credential-secret} (variável de ambiente
 * {@code CAMERA_CREDENTIAL_SECRET}), nunca hardcoded no código.</p>
 */
@Component
public class CameraCredentialCipher {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CameraCredentialCipher(
            @Value("${app.camera.credential-secret:troque-este-segredo-em-producao}") String segredo) {
        this.secretKey = deriveKey(segredo);
    }

    /**
     * Cifra a senha em texto puro e retorna uma String Base64 contendo IV + texto cifrado.
     */
    public String cifrar(String senhaTextoPuro) {
        if (senhaTextoPuro == null || senhaTextoPuro.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] textoCifrado = cipher.doFinal(senhaTextoPuro.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = new byte[iv.length + textoCifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(textoCifrado, 0, resultado, iv.length, textoCifrado.length);

            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao cifrar credencial da câmera", e);
        }
    }

    /**
     * Decifra o valor armazenado no banco, retornando a senha original em texto puro.
     */
    public String decifrar(String valorCifrado) {
        if (valorCifrado == null || valorCifrado.isBlank()) {
            return null;
        }
        try {
            byte[] dados = Base64.getDecoder().decode(valorCifrado);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            System.arraycopy(dados, 0, iv, 0, iv.length);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] textoPuro = cipher.doFinal(dados, iv.length, dados.length - iv.length);

            return new String(textoPuro, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao decifrar credencial da câmera", e);
        }
    }

    private SecretKeySpec deriveKey(String segredo) {
        try {
            // Deriva uma chave de 256 bits a partir do segredo configurado via SHA-256,
            // permitindo que o segredo de origem tenha qualquer tamanho.
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] chave = sha256.digest(segredo.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(chave, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao derivar chave de criptografia de câmeras", e);
        }
    }
}
