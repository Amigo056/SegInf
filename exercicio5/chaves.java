
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.PublicKey;
import java.security.PrivateKey;

import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
//alinea b)
public class chaves {

    // Define os parâmetros do RSA-OAEP usados pelo JWE.
    // O algoritmo RSA-OAEP utiliza SHA-1 e MGF1 com SHA-1.
    private static final OAEPParameterSpec PARAMETROS_OAEP =
            new OAEPParameterSpec(
                    "SHA-1",
                    "MGF1",
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT
            );

    // Cifra uma chave AES com a chave pública RSA.
    public static byte[] cifrarChave(SecretKey chaveAES, PublicKey chavePublica) 
    
    throws Exception {

        // Cria o objeto de cifra RSA com OAEP.
        Cipher cifra = Cipher.getInstance("RSA/ECB/OAEPPadding");

        // Inicializa a cifra com a chave pública.
        cifra.init(
                Cipher.ENCRYPT_MODE,
                chavePublica,
                PARAMETROS_OAEP
        );

        // Obtém os bytes da chave AES.
        byte[] bytesChave = chaveAES.getEncoded();

        // Cifra os bytes da chave AES.
        byte[] chaveCifrada = cifra.doFinal(bytesChave);

        // Devolve a chave AES cifrada.
        return chaveCifrada;
    }

    // Recupera a chave AES com a chave privada RSA.
    public static SecretKey decifrarChave( byte[] chaveCifrada, PrivateKey chavePrivada) 
    throws Exception {

        // Cria o objeto de decifra RSA com OAEP.
        Cipher cifra = Cipher.getInstance("RSA/ECB/OAEPPadding");

        // Inicializa a decifra com a chave privada.
        cifra.init(
                Cipher.DECRYPT_MODE,
                chavePrivada,
                PARAMETROS_OAEP
        );

        // Recupera os bytes originais da chave AES.
        byte[] bytesChave = cifra.doFinal(chaveCifrada);

        // Reconstrói uma chave secreta AES a partir dos bytes.
        SecretKey chaveAES = new SecretKeySpec(
                bytesChave,
                "AES"
        );

        // Devolve a chave AES recuperada.
        return chaveAES;
    }
}

