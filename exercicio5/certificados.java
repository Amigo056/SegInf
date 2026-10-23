
import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;

import java.util.Arrays;
import java.util.Set;
//alinea c)
public class certificados {

    // Lê um certificado X.509 a partir de um ficheiro.
    public static X509Certificate lerCertificado(
            String caminho) throws Exception {

        // Cria uma fábrica de certificados X.509.
        CertificateFactory fabrica =
                CertificateFactory.getInstance("X.509");

        // Abre o ficheiro e lê o certificado.
        try (FileInputStream ficheiro =
                     new FileInputStream(caminho)) {

            return (X509Certificate)
                    fabrica.generateCertificate(ficheiro);
        }
    }

    // Valida o certificado de Alice através da cadeia PKIX.
    // Se a validação for bem-sucedida, devolve a chave pública.
    public static PublicKey obterChavePublica(
            String caminhoAlice,
            String caminhoIntermedio,
            String caminhoRaiz) throws Exception {

        // Lê os três certificados.
        X509Certificate alice =
                lerCertificado(caminhoAlice);

        X509Certificate intermedio =
                lerCertificado(caminhoIntermedio);

        X509Certificate raiz =
                lerCertificado(caminhoRaiz);

        // Cria a cadeia de certificação:
        // Alice -> CA intermédia.
        // A raiz fica separada como âncora de confiança.
        CertificateFactory fabrica =
                CertificateFactory.getInstance("X.509");

        CertPath cadeia = fabrica.generateCertPath(
                Arrays.asList(alice, intermedio)
        );

        // Define a CA raiz como âncora de confiança.
        TrustAnchor ancora = new TrustAnchor(raiz, null);

        // Configura os parâmetros de validação PKIX.
        PKIXParameters parametros =
                new PKIXParameters(Set.of(ancora));

        // A verificação de revogação fica desativada
        // neste teste porque não foram configurados
        // os mecanismos necessários para a realizar.
        parametros.setRevocationEnabled(false);

        // Cria o validador PKIX.
        CertPathValidator validador =
                CertPathValidator.getInstance("PKIX");

        // Valida a cadeia. Se falhar, lança uma exceção.
        validador.validate(cadeia, parametros);

        // Apenas devolve a chave pública após a validação.
        return alice.getPublicKey();
    }
}

