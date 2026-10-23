import java.security.PrivateKey;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

public class JWE {

    // Caminho dos certificados fornecidos pelo professor.
    private static final String PASTA =
            "files_certificates-keys/certificates-keys/";

    // Cria um token JWE destinado a Alice.
    public static String cifrarMensagem(
            String mensagem,
            String caminhoCertificado) throws Exception {

        // 1. Valida o certificado do destinatário
        // e obtém a respetiva chave pública RSA.
        PublicKey chavePublica =
                certificados.obterChavePublica(
                        caminhoCertificado,
                        PASTA + "intermediates/CA1-int.cer",
                        PASTA + "trust-anchors/CA1.cer"
                );

        // 2. Gera uma chave AES de 256 bits.
        // Esta chave é designada por CEK no JWE.
        KeyGenerator gerador = KeyGenerator.getInstance("AES");
        gerador.init(256);

        SecretKey chaveAES = gerador.generateKey();

        // 3. Cifra a chave AES com RSA-OAEP,
        // utilizando o componente da alínea b).
        byte[] chaveCifrada = chaves.cifrarChave(
                chaveAES,
                chavePublica
        );

        // 4. Cria o cabeçalho protegido do JWE.
        String cabecalho =
                "{\"alg\":\"RSA-OAEP\",\"enc\":\"A256GCM\"}";

        // 5. Codifica o cabeçalho em Base64URL,
        // sem os caracteres de padding '='.
        String cabecalhoCodificado =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                cabecalho.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        // 6. Gera um IV aleatório de 12 bytes.
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // 7. Define uma tag de autenticação de 128 bits.
        int tamanhoTag = 128;

        // 8. Cifra a mensagem com AES-GCM.
        //
        // No JWE, o AAD corresponde ao cabeçalho
        // protegido já codificado em Base64URL.
        cifra.Resultado resultado = cifra.cifrar(
                mensagem,
                cabecalhoCodificado,
                chaveAES,
                iv,
                tamanhoTag
        );

        // 9. Cria o codificador Base64URL sem padding.
        Base64.Encoder codificador =
                Base64.getUrlEncoder().withoutPadding();

        // 10. Codifica separadamente os componentes.
        String parteChave = codificador.encodeToString(
                chaveCifrada
        );

        String parteIV = codificador.encodeToString(iv);

        String parteCriptograma = codificador.encodeToString(
                resultado.criptograma()
        );

        String parteTag = codificador.encodeToString(
                resultado.tag()
        );

        // 11. Constrói o token JWE com cinco partes,
        // separadas pelo carácter ponto.
        String token = cabecalhoCodificado + "."
                + parteChave + "."
                + parteIV + "."
                + parteCriptograma + "."
                + parteTag;

        return token;
    }


    // Recupera a mensagem original a partir de um token JWE.
    public static String decifrarMensagem(
            String token,
            String caminhoPFX,
            char[] palavraPasse) throws Exception {

        // 1. Divide o token JWE nas cinco partes.
        // O ponto é o separador utilizado no formato compacto.
        String[] partes = token.split("\\.", -1);

        // Confirma que existem exatamente cinco partes.
        if (partes.length != 5) {
            throw new IllegalArgumentException(
                    "O token JWE deve conter cinco partes."
            );
        }

        // 2. Obtém cada uma das partes do token.
        String cabecalhoCodificado = partes[0];
        String chaveCodificada = partes[1];
        String ivCodificado = partes[2];
        String criptogramaCodificado = partes[3];
        String tagCodificada = partes[4];

        // 3. Cria um descodificador Base64URL.
        Base64.Decoder descodificador = Base64.getUrlDecoder();

        // Descodifica o cabeçalho protegido.
        String cabecalho = new String(
                descodificador.decode(cabecalhoCodificado),
                StandardCharsets.UTF_8
        );

        // Para este primeiro teste, aceitamos exatamente
        // o cabeçalho produzido pelo nosso programa.
        String cabecalhoEsperado =
                "{\"alg\":\"RSA-OAEP\",\"enc\":\"A256GCM\"}";

        if (!cabecalho.equals(cabecalhoEsperado)) {
            throw new IllegalArgumentException(
                    "Cabecalho JWE nao suportado."
            );
        }

        // 4. Descodifica os componentes binários do JWE.
        byte[] chaveCifrada =
                descodificador.decode(chaveCodificada);

        byte[] iv =
                descodificador.decode(ivCodificado);

        byte[] criptograma =
                descodificador.decode(criptogramaCodificado);

        byte[] tag =
                descodificador.decode(tagCodificada);

        // Verifica o tamanho do IV: 12 bytes = 96 bits.
        if (iv.length != 12) {
            throw new IllegalArgumentException(
                    "O IV deve ter 12 bytes."
            );
        }

        // Verifica o tamanho da tag: 16 bytes = 128 bits.
        if (tag.length != 16) {
            throw new IllegalArgumentException(
                    "A tag deve ter 16 bytes."
            );
        }

        // 5. Obtém a chave privada RSA do destinatário.
        // Utiliza o componente criado na alínea c).
        PrivateKey chavePrivada =
                FicheiroPFX.obterChavePrivada(
                        caminhoPFX,
                        palavraPasse
                );

        // 6. Recupera a chave AES usando RSA-OAEP.
        // Utiliza o componente criado na alínea b).
        SecretKey chaveAES = chaves.decifrarChave(
                chaveCifrada,
                chavePrivada
        );

        // A256GCM exige uma chave AES de 256 bits.
        if (chaveAES.getEncoded().length != 32) {
            throw new IllegalArgumentException(
                    "A chave AES deve ter 256 bits."
            );
        }

        // 7. Decifra o criptograma com AES-GCM.
        //
        // O AAD é o cabeçalho protegido em Base64URL,
        // exatamente como foi usado durante a cifra.
        //
        // O método decifrar() verifica também a tag.
        String mensagem = cifra.decifrar(
                criptograma,
                tag,
                cabecalhoCodificado,
                chaveAES,
                iv,
                128
        );

        // 8. Devolve a mensagem original recuperada.
        return mensagem;
    }

    // Ponto de entrada da aplicação de consola.
    
public static void main(String[] args) throws Exception {

    // Verifica se foram fornecidos três argumentos:
    // operação, texto/token e caminho do ficheiro.
    if (args.length != 3) {
        System.out.println("Utilizacao:");
        System.out.println(
                "java JWE enc <mensagem> <certificado>"
        );
        System.out.println(
                "java JWE dec <token> <ficheiro.pfx>"
        );
        return;
    }

    // Primeiro argumento: operação pretendida.
    String operacao = args[0];

    if (operacao.equals("enc")) {

        // Segundo argumento: mensagem original.
        String mensagem = args[1];

        // Terceiro argumento: certificado do destinatário.
        String certificado = args[2];

        // Produz o token JWE.
        String token = cifrarMensagem(
                mensagem,
                certificado
        );

        // Apresenta o resultado.
        System.out.println("JWE token = " + token);

    } else if (operacao.equals("dec")) {

        // Segundo argumento: token JWE.
        String token = args[1];

        // Terceiro argumento: keystore do destinatário.
        String ficheiroPFX = args[2];

        // Tenta ler a palavra-passe sem a mostrar.
        Console consola = System.console();
        char[] palavraPasse;

        if (consola != null) {
            palavraPasse = consola.readPassword(
                    "Palavra-passe do PFX: "
            );
        } else {
            // Alternativa para ambientes sem Console.
            Scanner teclado = new Scanner(System.in);
            System.out.print("Palavra-passe do PFX: ");
            palavraPasse = teclado.nextLine().toCharArray();
        }

        try {

            // Decifra o token utilizando a chave privada.
            String mensagem = decifrarMensagem(
                    token,
                    ficheiroPFX,
                    palavraPasse
            );

            // Apresenta a mensagem recuperada.
            System.out.println(
                    "Decrypted text = " + mensagem
            );

        } finally {

            // Limpa o array que guarda a palavra-passe.
            Arrays.fill(palavraPasse, '\0');
        }

    } else {

        // A operação fornecida não é suportada.
        System.out.println("Operacao desconhecida.");
        System.out.println("Utilize 'enc' ou 'dec'.");
    }
}
}
