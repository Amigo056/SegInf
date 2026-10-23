
import javax.crypto.AEADBadTagException;

import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class TesteAlteracaoJWE {

    public static void main(String[] args) throws Exception {

        // 1. Define a mensagem que queremos proteger.
        String mensagem =
                "Este trabalho pertence ao grupo 3 da turma 51D";

        // 2. Define os caminhos dos ficheiros de Alice.
        String pasta =
                "files_certificates-keys/certificates-keys/";

        String certificado =
                pasta + "end-entities/Alice_1.cer";

        String ficheiroPFX =
                pasta + "pfx/Alice_1.pfx";

        // 3. Cria um JWE válido utilizando a chave pública
        // obtida do certificado de Alice.
        String tokenOriginal = JWE.cifrarMensagem(
                mensagem,
                certificado
        );

        System.out.println("JWE original criado com sucesso.");

        // 4. Pede a palavra-passe do PFX para obter
        // a chave privada de Alice.
        Scanner teclado = new Scanner(System.in);

        System.out.print("Palavra-passe do PFX: ");
        char[] palavraPasse =
                teclado.nextLine().toCharArray();

        try {

            // ----------------------------------------
            // TESTE 1: TOKEN ORIGINAL
            // ----------------------------------------

            // Decifra o token sem qualquer alteração.
            String mensagemRecuperada =
                    JWE.decifrarMensagem(
                            tokenOriginal,
                            ficheiroPFX,
                            palavraPasse
                    );

            System.out.println("\nTESTE 1 - Token original");

            System.out.println(
                    "Mensagem recuperada: " + mensagemRecuperada
            );

            // ----------------------------------------
            // TESTE 2: TOKEN MODIFICADO
            // ----------------------------------------

            // Divide o JWE nas suas cinco partes.
            String[] partes = tokenOriginal.split("\\.", -1);

            // A quarta parte (índice 3) contém
            // o criptograma em Base64URL.
            byte[] criptograma = Base64.getUrlDecoder()
                    .decode(partes[3]);

            // Verifica que existe pelo menos um byte
            // para podermos realizar a alteração.
            if (criptograma.length == 0) {
                throw new IllegalArgumentException(
                        "O criptograma esta vazio."
                );
            }

            // Altera um bit do primeiro byte.
            // Isto simula uma modificação não autorizada.
            criptograma[0] ^= 1;

            // Volta a codificar o criptograma alterado
            // em Base64URL, sem padding.
            partes[3] = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(criptograma);

            // Reconstrói o JWE com o criptograma alterado.
            // As outras quatro partes permanecem iguais.
            String tokenAlterado = String.join(".", partes);

            System.out.println("\nTESTE 2 - Token alterado");

            try {

                // Tenta decifrar o token modificado.
                JWE.decifrarMensagem(
                        tokenAlterado,
                        ficheiroPFX,
                        palavraPasse
                );

                // Não deverá chegar aqui.
                System.out.println(
                        "ERRO: o token alterado foi aceite!"
                );

            } catch (AEADBadTagException e) {

                // Resultado esperado:
                // AES-GCM rejeitou os dados modificados.
                System.out.println(
                        "Autenticacao falhou!"
                );

                System.out.println(
                        "O AES-GCM detetou a alteracao do criptograma."
                );
            }

        } finally {

            // Apaga a palavra-passe guardada no array.
            Arrays.fill(palavraPasse, '\0');
        }
    }
}
