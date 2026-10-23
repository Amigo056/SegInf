import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/** Questao 4.1: cem insercoes, cada uma numa nova execucao de Blockchain. */
public class Experiencia41 {
    private static final int QUANTIDADE = 100;
    private static final int ALVO = 512; //1024;

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Uso: java Experiencia41 [pasta-nova-para-resultados]");
            System.exit(1);
        }

        try {
            String instante = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            Path pasta = args.length == 1 ? Path.of(args[0])
                    : Path.of("resultados_4_1", "execucao_" + instante);
            executarExperiencia(pasta, gerarMensagens(QUANTIDADE));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Erro: a experiencia foi interrompida.");
            System.exit(1);
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }

    /** Mensagens de 64 caracteres hexadecimais, obtidas de 32 bytes aleatorios. */
    private static List<String> gerarMensagens(int quantidade) {
        SecureRandom aleatorio = new SecureRandom();
        List<String> mensagens = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            byte[] dados = new byte[32];
            aleatorio.nextBytes(dados);
            mensagens.add(HexFormat.of().formatHex(dados));
        }
        return mensagens;
    }

    /** Separado de main para permitir testes funcionais com uma cadeia curta. */
    static void executarExperiencia(Path pasta, List<String> mensagens)
            throws IOException, InterruptedException, URISyntaxException {
        if (mensagens.isEmpty()) {
            throw new IllegalArgumentException("A experiencia precisa de mensagens.");
        }

        pasta = pasta.toAbsolutePath().normalize();
        Files.createDirectories(pasta.getParent());
        // A pasta tem de ser nova: cada experiencia parte de uma cadeia vazia.
        Files.createDirectory(pasta);
        Path blocos = Files.createDirectory(pasta.resolve("blocos"));
        Path logs = Files.createDirectory(pasta.resolve("logs"));
        Path csv = pasta.resolve("resultados.csv");
        Files.write(pasta.resolve("mensagens.txt"), mensagens, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        boolean windows = System.getProperty("os.name").startsWith("Windows");
        Path java = Path.of(System.getProperty("java.home"), "bin",
                windows ? "java.exe" : "java");
        Path classes = Path.of(Blockchain.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());

        long totalTentativas = 0;
        double totalAplicacaoMs = 0;
        double totalProcessoMs = 0;
        int hashAnterior = 0;

        System.out.println("Resultados: " + pasta);
        System.out.println("Insercoes: " + mensagens.size() + " | Alvo: " + ALVO);

        try (BufferedWriter tabela = Files.newBufferedWriter(csv, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            tabela.write("bloco,alvo,nonce,h16,tentativas,tempo_aplicacao_ms,tempo_processo_ms");
            tabela.newLine();

            for (int i = 0; i < mensagens.size(); i++) {
                int indice = i + 1;
                Path log = logs.resolve(indice + ".log");
                ProcessBuilder comando = new ProcessBuilder(java.toString(), "-cp",
                        classes.toString(), "Blockchain", mensagens.get(i));
                comando.directory(blocos.toFile());
                comando.redirectErrorStream(true);
                comando.redirectOutput(log.toFile());

                long inicio = System.nanoTime();
                Process processo = comando.start();
                int codigo;
                try {
                    codigo = processo.waitFor();
                } catch (InterruptedException e) {
                    processo.destroyForcibly();
                    throw e;
                }
                double processoMs = (System.nanoTime() - inicio) / 1_000_000.0;

                String saida = Files.readString(log, StandardCharsets.UTF_8);
                if (codigo != 0) {
                    throw new IOException("Falhou a insercao " + indice
                            + ". Consulte " + log + ".");
                }

                int blocoObtido = Integer.parseInt(campo(saida, "Bloco criado: "));
                int alvoObtido = Integer.parseInt(campo(saida, "Alvo: "));
                int anteriorObtido = Integer.parseInt(campo(saida, "Hash anterior: "));
                long nonce = Long.parseLong(campo(saida, "Nonce: "));
                int hash = Integer.parseInt(campo(saida, "H16: ").split(" ")[0]);
                long tentativas = Long.parseLong(campo(saida, "Tentativas: "));
                double aplicacaoMs = Double.parseDouble(
                        campo(saida, "Tempo de execucao: ").replace(" ms", ""));

                if (blocoObtido != indice || alvoObtido != ALVO
                        || anteriorObtido != hashAnterior || hash < 0 || hash >= ALVO
                        || nonce < 0 || tentativas != nonce + 1
                        || !Double.isFinite(aplicacaoMs) || aplicacaoMs < 0) {
                    throw new IOException("Resultado inesperado na insercao " + indice
                            + ". Consulte " + log + ".");
                }

                tabela.write(String.format(Locale.ROOT, "%d,%d,%d,%d,%d,%.3f,%.3f%n",
                        indice, ALVO, nonce, hash, tentativas, aplicacaoMs, processoMs));
                tabela.flush();
                totalTentativas += tentativas;
                totalAplicacaoMs += aplicacaoMs;
                totalProcessoMs += processoMs;
                hashAnterior = hash;

                System.out.printf(Locale.ROOT,
                        "Bloco %03d/%d | tentativas: %d | aplicacao: %.3f ms | processo: %.3f ms%n",
                        indice, mensagens.size(), tentativas, aplicacaoMs, processoMs);
            }
        }

        int quantidade = mensagens.size();
        String resumo = String.format(Locale.ROOT,
                "Experiencia 4.1%n"
                        + "Blocos inseridos: %d%n"
                        + "Alvo: %d%n"
                        + "Java: %s%n"
                        + "Sistema: %s (%s)%n"
                        + "Total de tentativas: %d%n"
                        + "Media de tentativas por bloco: %.3f%n"
                        + "Tempo medio da aplicacao: %.3f ms%n"
                        + "Tempo medio do processo: %.3f ms%n"
                        + "Aplicacao: de main ate a escrita dos ficheiros, sem arranque da JVM.%n"
                        + "Processo: inclui lancamento da JVM, aplicacao e escrita da saida no log.%n",
                quantidade, ALVO, System.getProperty("java.version"),
                System.getProperty("os.name"), System.getProperty("os.arch"),
                totalTentativas, (double) totalTentativas / quantidade,
                totalAplicacaoMs / quantidade, totalProcessoMs / quantidade);

        Files.writeString(pasta.resolve("resumo.txt"), resumo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        System.out.println();
        System.out.print(resumo);
        System.out.println("Tabela completa: " + csv);
    }

    private static String campo(String saida, String prefixo) throws IOException {
        for (String linha : saida.split("\\R")) {
            if (linha.startsWith(prefixo)) {
                return linha.substring(prefixo.length()).trim();
            }
        }
        throw new IOException("Campo em falta na saida de Blockchain: " + prefixo);
    }
}
