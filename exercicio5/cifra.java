import javax.crypto.Cipher;  // permite utilizar a classe responsável pelas operaçoes criptograficas de cifra e decifra
import javax.crypto.SecretKey; //permite trabalhar com uma chave secreta, neste caso uma chave AES
import javax.crypto.spec.GCMParameterSpec; // configurar os parametros necessarios ao modo GCM, nomeadamente IV e o tamanho da tag
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
//alinea a)
public class cifra {
    // Guarda os dois resultados prodizidos pela cifra
    // A tag é necessária para verificar a autentidade
    public record Resultado(byte[] criptograma, byte[] tag) {
    }

    public static Resultado cifrar(
        String mensagem,
        String dadosAdicionais, 
        SecretKey chave,
        byte[] iv,
        int tamanhoTag) 
        
    
    throws Exception {

        //Seleciona o algoritmo AES com o modo GCM.
        Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");

        //Define os parametros GCM: tamanho em bits e vetor de inicialização (IV).
        GCMParameterSpec parametros = new GCMParameterSpec(tamanhoTag, iv);   

        // Inicializa o objeto a cifrar 
        cifra.init(Cipher.ENCRYPT_MODE, chave, parametros);

        //Autentica os dados adicionais sem os cifrar
        cifra.updateAAD(dadosAdicionais.getBytes(StandardCharsets.UTF_8) );

        //Converte a mensagem em bytes e realiza a cifra
        // O resultadi contºem o criptograma seguido da tag
        byte[] resultado = cifra.doFinal(mensagem.getBytes(StandardCharsets.UTF_8));

        //Calcula o tamanho da tag em bytes
        int bytesTag = tamanhoTag / 8;

        // Separa o criptograma da tag de autenticação
        byte[] criptograma = Arrays.copyOfRange(resultado, 0, resultado.length - bytesTag);

        byte[] tag = Arrays.copyOfRange(resultado, resultado.length - bytesTag, resultado.length);

        // retorna/ devolve os dois elementos separadamente
        return new Resultado (criptograma, tag);
        }

        public static String decifrar(
            byte[] criptograma,
            byte[] tag,
            String dadosAdicionais,
            SecretKey chave,
            byte[] iv,
            int tamanhoTag)
        throws Exception {

            //Seleciona novamente o algoritmo AES-GCM
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");

            //Utiliza os mesmos parametros usados na cifra
            GCMParameterSpec parametros = new GCMParameterSpec(tamanhoTag, iv);

            // Inicializa o objeto a decifrar
            cifra.init(Cipher.DECRYPT_MODE, chave, parametros);

            //Fornece os mesmos dados adicionais autenticados
            cifra.updateAAD(dadosAdicionais.getBytes(StandardCharsets.US_ASCII));

            //Junta o criptograma e a tag, pela mesma ordem utilizada no resultafi da operação cifra
            byte[] dados = new byte[criptograma.length + tag.length];

            System.arraycopy(criptograma, 0, dados, 0, criptograma.length);
            System.arraycopy(tag, 0, dados, criptograma.length, tag.length);

            //Decifra e verifica a autenciadade, se a tag for invalida, a operação lança exceção

            byte[] mensagem = cifra.doFinal(dados);

            //converte os bytes recuperados novamente para texto

            return new String(mensagem, StandardCharsets.UTF_8);
        }
}
