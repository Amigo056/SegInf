
import java.util.Arrays;
import java.util.Scanner;

public class TesteJWE {

    public static void main(String[] args) throws Exception {

        // 1. Define a mensagem original.
        String mensagem =
                "Este trabalho pertence ao grupo 3 da turma 51D";

        // 2. Define a pasta dos certificados.
        String pasta =
                "files_certificates-keys/certificates-keys/";

        // Certificado de Alice: contém a chave pública.
        String caminhoCertificado =
                pasta + "end-entities/Alice_1.cer";

        // Ficheiro PFX de Alice: contém a chave privada.
        String caminhoPFX =
                pasta + "pfx/Alice_1.pfx";

        // 3. Cifra a mensagem e cria o token JWE.
        String token = JWE.cifrarMensagem(
                mensagem,
                caminhoCertificado
        );

        System.out.println("JWE token = " + token);

        // 4. Pede a palavra-passe do PFX ao utilizador.
        Scanner teclado = new Scanner(System.in);

        System.out.print("\nPalavra-passe do PFX: ");

        // Neste teste, a palavra-passe será visível.
        char[] palavraPasse =
                teclado.nextLine().toCharArray();

        String mensagemRecuperada;

        try {

            // 5. Decifra o token JWE criado anteriormente.
            mensagemRecuperada = JWE.decifrarMensagem(
                    token,
                    caminhoPFX,
                    palavraPasse
            );

        } finally {

            // Apaga a palavra-passe do array.
            Arrays.fill(palavraPasse, '\0');
        }

        // 6. Apresenta a mensagem original recuperada.
        System.out.println(
                "Mensagem recuperada: " + mensagemRecuperada
        );

        // 7. Compara a mensagem original com a recuperada.
        boolean mensagensIguais =
                mensagem.equals(mensagemRecuperada);

        System.out.println(
                "Mensagens iguais: " + mensagensIguais
        );
    }
}
