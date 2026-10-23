
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Enumeration;
//alinea c)
public class FicheiroPFX {

    // Obtém uma chave privada guardada num ficheiro PFX.
    public static PrivateKey obterChavePrivada(String caminho,char[] palavraPasse) 
    throws Exception {

        // Cria um keystore no formato PKCS#12.
        KeyStore keystore = KeyStore.getInstance("PKCS12");

        // Abre o ficheiro PFX e carrega o seu conteúdo.
        // A palavra-passe permite aceder ao keystore.
        try (FileInputStream ficheiro =
                     new FileInputStream(caminho)) {keystore.load(ficheiro, palavraPasse);}

        // Obtém os nomes (aliases) das entradas.
        Enumeration<String> aliases = keystore.aliases();

        // Percorre as entradas do keystore.
        while (aliases.hasMoreElements()) {

            // Obtém o nome da entrada atual.
            String alias = aliases.nextElement();

            // Verifica se esta entrada guarda uma chave.
            if (keystore.isKeyEntry(alias)) {

                // Tenta recuperar a chave da entrada.
                Key chave = keystore.getKey(
                        alias, palavraPasse
                );

                // Confirma que se trata de uma chave privada.
                if (chave instanceof PrivateKey) {

                    // Devolve a chave privada encontrada.
                    return (PrivateKey) chave;
                }
            }
        }

        // Caso não exista uma chave privada no keystore.
        throw new Exception(
                "Nao foi encontrada uma chave privada no PFX."
        );
    }
}

