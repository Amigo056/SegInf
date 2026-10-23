
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.AEADBadTagException;

public class TesteCifra {
    
    public static void main(String[] arg) throws Exception{

        //mensagem que pretendemos decifrar
        String mensagem = "Este trabalho pertence ao grupo 3 da turma 51D";

        //dados adicionais que serão autenticados
        String dadosAdicionais = "Grupo 3 - Turma 51D - Trabalho 1 de Seguranca Informatica";

        //Cria o tamanho da chave: 138 bits
        KeyGenerator geradorChave = KeyGenerator.getInstance("AES");

        //Define o tamanho da chave: 256 bits
        geradorChave.init(256);

        //Gera a chave secreta
        SecretKey chave = geradorChave.generateKey();

        //Cria im IV de 12 bytes, equivalente a 96 bits
        byte[] iv = new byte[12];

        // Prenche o IV com bytes aleatórios
        new SecureRandom().nextBytes(iv);

        //Define o tamanho da tag em bits
        int tamanhoTag = 128;

        //Cifra a mensagem
        cifra.Resultado resultado =  cifra.cifrar(
            mensagem,
            dadosAdicionais,
            chave,
            iv,
            tamanhoTag);

            //Apresenta o criptograma em base64
            System.out.println("criptograma: " + Base64.getEncoder().encodeToString(resultado.criptograma()));


            //apresenta a tag em base64
            System.out.println("Tag: " + Base64.getEncoder().encodeToString(resultado.tag()));

            // Decifra a mensagem com os mesmos parâmetros.
            String mensagemRecuperada = cifra.decifrar(
                resultado.criptograma(),
                resultado.tag(),
                dadosAdicionais,
                chave,
                iv,
                tamanhoTag);

            // Mostra a mensagem original recuperada.
            System.out.println("Mensagem recuperada: " + mensagemRecuperada);


        // Cria uma cópia do criptograma original.
        // Assim, não alteramos os dados do primeiro teste.
        byte[] criptogramaAlterado = resultado.criptograma().clone();

        // Modifica intencionalmente o primeiro byte.
        // O operador XOR altera um bit desse byte.
        criptogramaAlterado[0] ^= 1;

        System.out.println("\nTeste com criptograma alterado:");

        try {

            // Tenta decifrar o criptograma modificado.
            // Mantemos a chave, o IV, o AAD e a tag originais.
            String mensagemAlterada = cifra.decifrar(
                    criptogramaAlterado,
                    resultado.tag(),
                    dadosAdicionais,
                    chave,
                    iv,
                    tamanhoTag
            );

            // Esta linha não deverá ser executada,
            // porque a autenticação deverá falhar.
            System.out.println(
                    "Mensagem recuperada: " + mensagemAlterada
            );

        } catch (AEADBadTagException e) {

            // O AES-GCM detetou que os dados não correspondem
            // à marca de autenticação original.
            System.out.println(
                    "Autenticacao falhou: o criptograma foi alterado."
            );
        }
 
    }

}
