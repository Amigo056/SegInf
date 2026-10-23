
import java.security.PublicKey;

public class Testecertificados {

    public static void main(String[] args) throws Exception {

        // Caminho da pasta que contém os certificados.
        // Este exemplo assume que executamos o programa
        // a partir da pasta principal Trabalho1.
        String pasta =
                "files_certificates-keys/certificates-keys/";

        // Caminho do certificado de Alice.
        String alice =
                pasta + "end-entities/Alice_1.cer";

        // Caminho do certificado intermédio.
        String intermedio =
                pasta + "intermediates/CA1-int.cer";

        // Caminho do certificado raiz.
        String raiz =
                pasta + "trust-anchors/CA1.cer";

        // Valida o certificado e obtém a chave pública.
        PublicKey chavePublica =
                certificados.obterChavePublica(
                        alice,
                        intermedio,
                        raiz
                );

        // Apresenta os resultados.
        System.out.println("Certificado validado com sucesso!");

        System.out.println(
                "Algoritmo da chave publica: "
                        + chavePublica.getAlgorithm()
        );
    }
}
