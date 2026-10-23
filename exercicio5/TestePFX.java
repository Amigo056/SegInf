
import java.security.PrivateKey;
import java.util.Scanner;

public class TestePFX {

    public static void main(String[] args) throws Exception {

        // Localização dos certificados e keystores.
        String pasta =
                "files_certificates-keys/certificates-keys/";

        // Caminho do ficheiro PFX de Alice.
        String caminhoPFX =
                pasta + "pfx/Alice_1.pfx";

        // Lê a palavra-passe do teclado.
        // Para este teste, ela será visível no terminal.
        Scanner teclado = new Scanner(System.in);

        System.out.print("Palavra-passe do PFX: ");
        char[] palavraPasse =
                teclado.nextLine().toCharArray();

        try {

            // Recupera a chave privada do ficheiro PFX.
            PrivateKey chavePrivada =
                    FicheiroPFX.obterChavePrivada(
                            caminhoPFX,
                            palavraPasse
                    );

            // Apresenta apenas o algoritmo da chave.
            // Não devemos imprimir o seu conteúdo.
            System.out.println(
                    "Chave privada recuperada com sucesso!"
            );

            System.out.println(
                    "Algoritmo da chave privada: "
                            + chavePrivada.getAlgorithm()
            );

        } finally {

            // Limpa o array que contém a palavra-passe.
            java.util.Arrays.fill(palavraPasse, '\0');
        }
    }
}

