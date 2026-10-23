import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Acrescenta uma mensagem a uma cadeia de blocos guardada na diretoria atual. */
public class Blockchain {
    private static final int ALVO = 512; //1024;
    private static final Pattern FICHEIRO_BLOCO =
            Pattern.compile("([1-9][0-9]*)\\.(txt|nce|hsh)");

    public static void main(String[] args) {
        long inicio = System.nanoTime();

        if (args.length != 1) {
            System.err.println("Uso: java Blockchain \"mensagem a guardar\"");
            System.exit(1);
        }

        try {
            Path diretoria = Path.of(".").toAbsolutePath().normalize();
            int indice = proximoIndice(diretoria);
            int hashAnterior = lerHashAnterior(diretoria, indice);
            byte[] mensagem = args[0].getBytes(StandardCharsets.UTF_8);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            long nonce = 0;
            long tentativas = 0;
            int hash;

            // A mensagem e o hash anterior ficam fixos; varia-se apenas o nonce.
            while (true) {
                tentativas++;
                hash = calcularH16(sha256, mensagem, nonce, hashAnterior);
                if (hash < ALVO) {
                    break;
                }
                if (nonce == Long.MAX_VALUE) {
                    throw new IllegalArgumentException("Esgotaram-se os nonces disponiveis.");
                }
                nonce++;
            }

            guardarBloco(diretoria, indice, args[0], nonce, hash);
            double tempoMs = (System.nanoTime() - inicio) / 1_000_000.0;

            System.out.println("Bloco criado: " + indice);
            System.out.println("Ficheiros: " + indice + ".txt, " + indice
                    + ".nce, " + indice + ".hsh");
            System.out.println("Hash anterior: " + hashAnterior);
            System.out.println("Alvo: " + ALVO);
            System.out.println("Nonce: " + nonce);
            System.out.printf(Locale.ROOT, "H16: %d (0x%04X)%n", hash, hash);
            System.out.println("Tentativas: " + tentativas);
            System.out.printf(Locale.ROOT, "Tempo de execucao: %.3f ms%n", tempoMs);
        } catch (IOException | NoSuchAlgorithmException | IllegalArgumentException e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }

    /** Os blocos comecam em 1 e devem ter os tres ficheiros, sem saltar indices. */
    private static int proximoIndice(Path diretoria) throws IOException {
        TreeSet<Integer> indices = new TreeSet<>();

        try (DirectoryStream<Path> ficheiros = Files.newDirectoryStream(diretoria)) {
            for (Path ficheiro : ficheiros) {
                Matcher resultado = FICHEIRO_BLOCO.matcher(
                        ficheiro.getFileName().toString());
                if (resultado.matches()) {
                    indices.add(Integer.parseInt(resultado.group(1)));
                }
            }
        }

        int esperado = 1;
        for (int indice : indices) {
            if (indice != esperado) {
                throw new IOException("Numeracao incompleta: falta o bloco " + esperado + ".");
            }
            for (String extensao : new String[]{"txt", "nce", "hsh"}) {
                Path ficheiro = diretoria.resolve(indice + "." + extensao);
                if (!Files.isRegularFile(ficheiro)) {
                    throw new IOException("Bloco incompleto: falta " + ficheiro.getFileName() + ".");
                }
            }
            if (indice == Integer.MAX_VALUE) {
                throw new IOException("Esgotaram-se os indices disponiveis.");
            }
            esperado++;
        }
        return esperado;
    }

    private static int lerHashAnterior(Path diretoria, int indice) throws IOException {
        if (indice == 1) {
            return 0;
        }

        Path ficheiro = diretoria.resolve((indice - 1) + ".hsh");
        int hash;
        try {
            hash = Integer.parseInt(Files.readString(ficheiro, StandardCharsets.UTF_8).trim());
        } catch (NumberFormatException e) {
            throw new IOException("Hash invalido em " + ficheiro.getFileName() + ".", e);
        }
        if (hash < 0 || hash >= ALVO) {
            throw new IOException("O hash anterior nao cumpre o alvo atual: " + hash + ".");
        }
        return hash;
    }

    /**
     * Entrada do SHA256: mensagem UTF-8 || nonce (8 bytes) || hash anterior (2 bytes).
     * Os numeros usam big-endian. Os tamanhos fixos evitam ambiguidades na concatenacao.
     */
    private static int calcularH16(MessageDigest sha256, byte[] mensagem,
                                  long nonce, int hashAnterior) {
        byte[] numeros = ByteBuffer.allocate(Long.BYTES + Short.BYTES)
                .putLong(nonce)
                .putShort((short) hashAnterior)
                .array();

        sha256.update(mensagem);
        byte[] hashCompleto = sha256.digest(numeros);

        // Os dois primeiros bytes sao interpretados sem sinal: 0 a 65535.
        return ((hashCompleto[0] & 0xFF) << 8) | (hashCompleto[1] & 0xFF);
    }

    private static void guardarBloco(Path diretoria, int indice, String mensagem,
                                    long nonce, int hash) throws IOException {
        // CREATE_NEW impede que um ficheiro ja existente seja sobrescrito.
        escreverNovo(diretoria.resolve(indice + ".txt"), mensagem);
        escreverNovo(diretoria.resolve(indice + ".nce"), Long.toString(nonce));
        escreverNovo(diretoria.resolve(indice + ".hsh"), Integer.toString(hash));
    }

    private static void escreverNovo(Path ficheiro, String conteudo) throws IOException {
        Files.writeString(ficheiro, conteudo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }
}
