
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.PublicKey;
import java.security.PrivateKey;

import java.util.Arrays;
import java.util.Scanner;

public class TesteAliceChave {

    public static void main(String[] args) throws Exception {

        // 1. Define o caminho da pasta com os certificados.
        String pasta =
                "files_certificates-keys/certificates-keys/";

        // Caminho do certificado X.509 de Alice.
        String caminhoAlice =
                pasta + "end-entities/Alice_1.cer";

        // Caminho do certificado da autoridade intermédia.
        String caminhoIntermedio =
                pasta + "intermediates/CA1-int.cer";

        // Caminho da autoridade certificadora raiz.
        String caminhoRaiz =
                pasta + "trust-anchors/CA1.cer";

        // Caminho do keystore PKCS#12 de Alice.
        String caminhoPFX =
                pasta + "pfx/Alice_1.pfx";

        // 2. Valida o certificado X.509 de Alice.
        // Obtém a chave pública apenas se a validação
        // da cadeia de certificação for bem-sucedida.
        PublicKey chavePublica =
                certificados.obterChavePublica(
                        caminhoAlice,
                        caminhoIntermedio,
                        caminhoRaiz
                );

        System.out.println(
                "Certificado de Alice validado com sucesso!"
        );

        // 3. Obtém a palavra-passe do ficheiro PFX.
        Scanner teclado = new Scanner(System.in);

        System.out.print("Palavra-passe do PFX: ");
        char[] palavraPasse =
                teclado.nextLine().toCharArray();

        // 4. Recupera a chave privada de Alice.
        PrivateKey chavePrivada;

        try {
            chavePrivada = FicheiroPFX.obterChavePrivada(
                    caminhoPFX,
                    palavraPasse
            );

        } finally {
            // Limpa o array que guardou a palavra-passe.
            Arrays.fill(palavraPasse, '\0');
        }

        System.out.println(
                "Chave privada recuperada com sucesso!"
        );

        // 5. Gera uma chave AES de 256 bits.
        // Esta chave será protegida com RSA-OAEP.
        KeyGenerator geradorAES =
                KeyGenerator.getInstance("AES");

        geradorAES.init(256);

        SecretKey chaveAES = geradorAES.generateKey();

        // 6. Cifra a chave AES com a chave pública de Alice.
        // Utiliza o método implementado na alínea b).
        byte[] chaveCifrada = chaves.cifrarChave(
                chaveAES,
                chavePublica
        );

        System.out.println(
                "Chave AES cifrada com a chave publica de Alice."
        );

        // 7. Decifra a chave AES com a chave privada de Alice.
        // A chave privada foi obtida do ficheiro PFX.
        SecretKey chaveRecuperada = chaves.decifrarChave(
                chaveCifrada,
                chavePrivada
        );

        // 8. Compara os bytes da chave AES original
        // com os bytes da chave AES recuperada.
        boolean chavesIguais = Arrays.equals(
                chaveAES.getEncoded(),
                chaveRecuperada.getEncoded()
        );

        // 9. Apresenta o resultado do teste.
        System.out.println(
                "Chaves AES iguais: " + chavesIguais
        );
    }
}
