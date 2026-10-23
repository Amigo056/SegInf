import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.util.Arrays;
import java.util.Base64;

public class Testechave {

    public static void main(String[] args) throws Exception {

        // 1. GERAR A CHAVE AES

        // Cria um gerador de chaves AES.
        KeyGenerator geradorAES =
                KeyGenerator.getInstance("AES");

        // Define uma chave AES de 256 bits.
        geradorAES.init(256);

        // Gera a chave AES que pretendemos proteger.
        SecretKey chaveAES = geradorAES.generateKey();

        // 2. GERAR O PAR DE CHAVES RSA

        // Cria um gerador de pares de chaves RSA.
        KeyPairGenerator geradorRSA =
                KeyPairGenerator.getInstance("RSA");

        // Define o tamanho das chaves RSA: 2048 bits.
        geradorRSA.initialize(2048);

        // Gera o par de chaves RSA.
        KeyPair parChaves = geradorRSA.generateKeyPair();

        // Obtém a chave pública RSA.
        PublicKey chavePublica = parChaves.getPublic();

        // Obtém a chave privada RSA.
        PrivateKey chavePrivada = parChaves.getPrivate();

        // 3. CIFRAR A CHAVE AES

        // Protege a chave AES utilizando a chave pública RSA.
        byte[] chaveCifrada = chaves.cifrarChave(
                chaveAES,
                chavePublica
        );

        // Apresenta a chave cifrada em Base64.
        System.out.println("Chave AES cifrada:");
        System.out.println(
                Base64.getEncoder().encodeToString(chaveCifrada)
        );

        // 4. DECIFRAR A CHAVE AES

        // Recupera a chave AES com a chave privada RSA.
        SecretKey chaveRecuperada = chaves.decifrarChave(
                chaveCifrada,
                chavePrivada
        );

        // 5. COMPARAR AS DUAS CHAVES

        // Compara os bytes da chave original com os
        // bytes da chave recuperada após a decifra.
        boolean chavesIguais = Arrays.equals(
                chaveAES.getEncoded(),
                chaveRecuperada.getEncoded()
        );

        System.out.println();
        System.out.println(
                "Chave AES recuperada corretamente: " + chavesIguais
        );
    }
}
