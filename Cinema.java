import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class Cinema {

    /* ==================================================================
     * Função principal
     * ================================================================== */

    public static void main(String[] args) {
        // Usa o padrão brasileiro nos números (vírgula decimal: R$ 15,00)
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));

        Scanner teclado = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("      SISTEMA DE RESERVAS DE CINEMA      ");
        System.out.println("=========================================");

        // Dados iniciais do cinema (fileiras e assentos entre 5 e 25)
        double valorIngresso = lerDoublePositivo(teclado, "Valor do ingresso (R$): ");
        int linhas = lerInteiro(teclado, "Quantidade de fileiras (5 a 25): ", 5, 25);
        int colunas = lerInteiro(teclado, "Assentos por fileira (5 a 25): ", 5, 25);

        // Cria as matrizes da sala com todos os assentos livres
        int[][] idades = new int[linhas][colunas];
        char[][] sexos = new char[linhas][colunas];
        limparReservas(idades, sexos);

        int opcao;
        do {
            opcao = mostrarMenu(teclado);
            switch (opcao) {
                case 1: carregarDados(teclado, idades, sexos); break;
                case 2: consultarAssento(teclado, idades, sexos, valorIngresso); break;
                case 3: reservarAssentos(teclado, idades, sexos); break;
                case 4: liberarAssentos(teclado, idades, sexos); break;
                case 5: mostrarMapa(idades); break;
                case 6: menuRelatorios(teclado, idades, sexos, valorIngresso); break;
                case 7: salvarDados(teclado, idades, sexos); break;
                case 8: mostrarIntegrantes(); break;
                case 9: System.out.println("\nEncerrando o programa. Até logo!"); break;
                default: System.out.println("Opção inválida.");
            }
        } while (opcao != 9);

        teclado.close();
    }

    /* ==================================================================
     * Menus
     * ================================================================== */

    /**
     * Mostra o menu principal e devolve a opção escolhida.
     */
    static int mostrarMenu(Scanner teclado) {
        System.out.println();
        System.out.println("============== MENU PRINCIPAL ==============");
        System.out.println(" 1. Carregar dados");
        System.out.println(" 2. Consultar situação de um assento");
        System.out.println(" 3. Fazer reservas de n assentos");
        System.out.println(" 4. Liberar reserva de n assentos");
        System.out.println(" 5. Visualizar mapa do cinema");
        System.out.println(" 6. Relatórios");
        System.out.println(" 7. Salvar dados");
        System.out.println(" 8. Integrantes");
        System.out.println(" 9. Sair");
        System.out.println("============================================");
        return lerInteiro(teclado, "Escolha uma opção: ", 1, 9);
    }

    /**
     * Submenu de relatórios (opção 6).
     */
    static void menuRelatorios(Scanner teclado, int[][] idades, char[][] sexos, double valorIngresso) {
        String opcao;
        do {
            System.out.println();
            System.out.println("---------------- RELATÓRIOS ----------------");
            System.out.println(" a. Listagem das reservas");
            System.out.println(" b. Assentos liberados e reservados");
            System.out.println(" c. Reservas por sexo");
            System.out.println(" d. Gráfico de pagantes e receita");
            System.out.println(" v. Voltar ao menu principal");
            System.out.println("--------------------------------------------");
            System.out.print("Escolha uma opção: ");
            opcao = teclado.nextLine().trim().toLowerCase();

            switch (opcao) {
                case "a": relatorioListagem(idades, sexos, valorIngresso); break;
                case "b": relatorioOcupacao(idades); break;
                case "c": relatorioPorSexo(idades, sexos); break;
                case "d": relatorioGrafico(idades, valorIngresso); break;
                case "v": break;
                default: System.out.println("Opção inválida.");
            }
        } while (!opcao.equals("v"));
    }

    /* ==================================================================
     * Opção 1 - Carregar dados
     * ================================================================== */

    /**
     * Lê um arquivo no formato "Assento,Sexo,Idade" e grava as reservas
     * nas matrizes. Linhas inválidas ou fora da sala são ignoradas e
     * informadas ao usuário.
     */
    static void carregarDados(Scanner teclado, int[][] idades, char[][] sexos) {
        System.out.print("Nome do arquivo: ");
        String nomeArquivo = limparNomeArquivo(teclado.nextLine());

        // Se não achou e o usuário não digitou extensão, tenta com ".csv"
        if (procurarArquivo(nomeArquivo) == null && !nomeArquivo.contains(".")) {
            nomeArquivo = nomeArquivo + ".csv";
        }

        File arquivo = procurarArquivo(nomeArquivo);
        if (arquivo == null) {
            System.out.println("Arquivo não encontrado: " + new File(nomeArquivo).getAbsolutePath());
            System.out.println("O programa está rodando na pasta: " + System.getProperty("user.dir"));
            System.out.println("Coloque o arquivo nessa pasta ou digite o caminho completo.");
            return;
        }
        nomeArquivo = arquivo.getPath();
        System.out.println("Lendo " + arquivo.getAbsolutePath());

        if (lerSimNao(teclado, "Apagar as reservas atuais antes de carregar? (S/N): ")) {
            limparReservas(idades, sexos);
        }

        int carregados = 0;
        int ignorados = 0;
        int numeroLinha = 0;

        try {
            // Abre o arquivo para leitura usando um buffer
            FileReader reader = new FileReader(nomeArquivo);
            BufferedReader buffer = new BufferedReader(reader);

            buffer.readLine(); // descarta a linha do cabeçalho
            numeroLinha = 1;

            // Lê o arquivo linha a linha até o final (readLine devolve null)
            String linhaArquivo;
            while ((linhaArquivo = buffer.readLine()) != null) {
                numeroLinha++;
                linhaArquivo = linhaArquivo.trim();

                if (linhaArquivo.isEmpty()) {
                    continue; // pula linhas em branco
                }

                if (processarLinhaArquivo(linhaArquivo, idades, sexos)) {
                    carregados++;
                } else {
                    System.out.println("  Linha " + numeroLinha + " ignorada: " + linhaArquivo);
                    ignorados++;
                }
            }

            buffer.close();
            reader.close();
            System.out.println(carregados + " reserva(s) carregada(s). " + ignorados + " linha(s) ignorada(s).");
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    /**
     * Interpreta uma linha do arquivo ("A2,F,12") e grava a reserva.
     * Devolve true se a linha era válida e o assento estava livre.
     */
    static boolean processarLinhaArquivo(String linhaArquivo, int[][] idades, char[][] sexos) {
        String[] partes = linhaArquivo.split(",");
        if (partes.length != 3) {
            return false;
        }

        int[] assento = converterAssento(partes[0], idades.length, idades[0].length);
        char sexo = converterSexo(partes[1]);
        int idade;
        try {
            idade = Integer.parseInt(partes[2].trim());
        } catch (NumberFormatException e) {
            return false;
        }

        if (assento == null || sexo == ' ' || idade < 0 || idade > 120) {
            return false;
        }

        int l = assento[0];
        int c = assento[1];
        if (estaReservado(idades, l, c)) {
            return false; // não sobrescreve reserva já existente
        }

        reservar(idades, sexos, l, c, sexo, idade);
        return true;
    }

    /* ==================================================================
     * Opção 2 - Consultar assento
     * ================================================================== */

    /**
     * Mostra se um assento está livre ou reservado e, se reservado,
     * o sexo, a idade e o valor pago pelo ocupante.
     */
    static void consultarAssento(Scanner teclado, int[][] idades, char[][] sexos, double valorIngresso) {
        int[] assento = lerAssento(teclado, "Assento (ex.: F5): ", idades.length, idades[0].length);
        int l = assento[0];
        int c = assento[1];
        String nome = nomeAssento(l, c);

        if (estaReservado(idades, l, c)) {
            int idade = idades[l][c];
            System.out.println("Assento " + nome + ": RESERVADO");
            System.out.println("  Sexo : " + descricaoSexo(sexos[l][c]));
            System.out.println("  Idade: " + idade + " anos");
            System.out.println("  Valor: " + formatarMoeda(calcularValor(idade, valorIngresso))
                    + " (" + categoriaIdade(idade) + ")");
        } else {
            System.out.println("Assento " + nome + ": LIBERADO");
        }
    }

    /* ==================================================================
     * Opção 3 - Reservar n assentos
     * ================================================================== */

    /**
     * Reserva n assentos consecutivos na mesma fileira a partir do
     * assento informado. Só reserva se todos estiverem disponíveis.
     */
    static void reservarAssentos(Scanner teclado, int[][] idades, char[][] sexos) {
        int colunas = idades[0].length;
        int[] inicio = lerAssento(teclado, "Assento inicial (ex.: F5): ", idades.length, colunas);
        int n = lerInteiro(teclado, "Quantidade de assentos: ", 1, colunas);
        int l = inicio[0];
        int c = inicio[1];

        // Verifica se os n assentos cabem na fileira
        if (c + n > colunas) {
            System.out.println("Não existem " + n + " assentos a partir de " + nomeAssento(l, c)
                    + " (a fileira termina em " + nomeAssento(l, colunas - 1) + "). Nenhuma reserva feita.");
            return;
        }

        // Verifica se todos estão livres (sem sobreposição)
        for (int i = c; i < c + n; i++) {
            if (estaReservado(idades, l, i)) {
                System.out.println("O assento " + nomeAssento(l, i)
                        + " já está reservado. Nenhuma reserva feita.");
                return;
            }
        }

        // Todos disponíveis: pede os dados de cada ocupante
        for (int i = c; i < c + n; i++) {
            System.out.println("-- Assento " + nomeAssento(l, i) + " --");
            char sexo = lerSexo(teclado, "  Sexo (M/F): ");
            int idade = lerInteiro(teclado, "  Idade: ", 0, 120);
            reservar(idades, sexos, l, i, sexo, idade);
        }
        System.out.println(n + " assento(s) reservado(s) com sucesso.");
    }

    /* ==================================================================
     * Opção 4 - Liberar n assentos
     * ================================================================== */

    /**
     * Libera n assentos consecutivos na mesma fileira a partir do
     * assento informado.
     */
    static void liberarAssentos(Scanner teclado, int[][] idades, char[][] sexos) {
        int colunas = idades[0].length;
        int[] inicio = lerAssento(teclado, "Assento inicial (ex.: F5): ", idades.length, colunas);
        int n = lerInteiro(teclado, "Quantidade de assentos: ", 1, colunas);
        int l = inicio[0];
        int c = inicio[1];

        if (c + n > colunas) {
            System.out.println("Não existem " + n + " assentos a partir de " + nomeAssento(l, c)
                    + ". Nenhum assento liberado.");
            return;
        }

        int liberados = 0;
        for (int i = c; i < c + n; i++) {
            if (estaReservado(idades, l, i)) {
                liberar(idades, sexos, l, i);
                liberados++;
            } else {
                System.out.println("  Assento " + nomeAssento(l, i) + " já estava liberado.");
            }
        }
        System.out.println(liberados + " reserva(s) liberada(s).");
    }

    /* ==================================================================
     * Opção 5 - Mapa do cinema
     * ================================================================== */

    /**
     * Mostra o mapa da sala: "·" para liberado e "X" para reservado.
     */
    static void mostrarMapa(int[][] idades) {
        System.out.println();
        System.out.println("Mapa de ocupação do cinema: (\"X\" = reservado e \"·\" = liberado)");

        // Cabeçalho com o número das colunas
        System.out.print("  ");
        for (int c = 0; c < idades[0].length; c++) {
            System.out.printf(" %02d", c + 1);
        }
        System.out.println();

        // Uma linha por fileira
        for (int l = 0; l < idades.length; l++) {
            System.out.print(letraFileira(l) + " ");
            for (int c = 0; c < idades[0].length; c++) {
                if (estaReservado(idades, l, c)) {
                    System.out.print("  X");
                } else {
                    System.out.print("  ·");
                }
            }
            System.out.println();
        }
    }

    /* ==================================================================
     * Opção 6 - Relatórios
     * ================================================================== */

    /**
     * a) Tabela com todas as reservas: assento, sexo, idade e valor.
     */
    static void relatorioListagem(int[][] idades, char[][] sexos, double valorIngresso) {
        if (contarReservados(idades) == 0) {
            System.out.println("Nenhuma reserva cadastrada.");
            return;
        }

        String separador = "+---------+-----------+-------+------------+";
        System.out.println(separador);
        System.out.println("| Assento | Sexo      | Idade | Valor      |");
        System.out.println(separador);
        for (int l = 0; l < idades.length; l++) {
            for (int c = 0; c < idades[0].length; c++) {
                if (estaReservado(idades, l, c)) {
                    int idade = idades[l][c];
                    System.out.printf("| %-7s | %-9s | %5d | %10s |%n",
                            nomeAssento(l, c), descricaoSexo(sexos[l][c]),
                            idade, formatarMoeda(calcularValor(idade, valorIngresso)));
                }
            }
        }
        System.out.println(separador);
    }

    /**
     * b) Total de assentos, divididos entre liberados e reservados.
     */
    static void relatorioOcupacao(int[][] idades) {
        int total = idades.length * idades[0].length;
        int reservados = contarReservados(idades);
        int liberados = total - reservados;

        System.out.println("Total de assentos : " + total);
        System.out.printf("Liberados         : %d (%.1f%%)%n", liberados, percentual(liberados, total));
        System.out.printf("Reservados        : %d (%.1f%%)%n", reservados, percentual(reservados, total));
    }

    /**
     * c) Quantidade de reservas por sexo.
     */
    static void relatorioPorSexo(int[][] idades, char[][] sexos) {
        int masculino = 0;
        int feminino = 0;
        for (int l = 0; l < idades.length; l++) {
            for (int c = 0; c < idades[0].length; c++) {
                if (estaReservado(idades, l, c)) {
                    if (sexos[l][c] == 'M') {
                        masculino++;
                    } else {
                        feminino++;
                    }
                }
            }
        }
        int total = masculino + feminino;

        System.out.printf("Masculino : %d (%.1f%%)%n", masculino, percentual(masculino, total));
        System.out.printf("Feminino  : %d (%.1f%%)%n", feminino, percentual(feminino, total));
        System.out.println("Total     : " + total);
    }

    /**
     * d) Gráfico de pagantes por faixa (meia menor, inteira, meia idoso)
     * com quantidade, percentual, valor arrecadado e receita total.
     */
    static void relatorioGrafico(int[][] idades, double valorIngresso) {
        int qtdMenor = 0, qtdInteira = 0, qtdIdoso = 0;
        double valorMenor = 0, valorInteira = 0, valorIdoso = 0;

        // Soma quantidade e valor de cada faixa
        for (int l = 0; l < idades.length; l++) {
            for (int c = 0; c < idades[0].length; c++) {
                if (estaReservado(idades, l, c)) {
                    int idade = idades[l][c];
                    double valor = calcularValor(idade, valorIngresso);
                    if (idade <= 17) {
                        qtdMenor++;
                        valorMenor += valor;
                    } else if (idade >= 60) {
                        qtdIdoso++;
                        valorIdoso += valor;
                    } else {
                        qtdInteira++;
                        valorInteira += valor;
                    }
                }
            }
        }

        int total = qtdMenor + qtdInteira + qtdIdoso;
        double receita = valorMenor + valorInteira + valorIdoso;

        System.out.println("Relatório gráfico:");
        imprimirLinhaGrafico("Meia menor", qtdMenor, total, valorMenor);
        imprimirLinhaGrafico("Inteira   ", qtdInteira, total, valorInteira);
        imprimirLinhaGrafico("Meia idoso", qtdIdoso, total, valorIdoso);
        imprimirLinhaGrafico("Total     ", total, total, receita);
    }

    /**
     * Imprime uma linha do gráfico. Exemplo:
     * Meia menor:  6 - 25,0% |=====---------------| R$ 90,00
     */
    static void imprimirLinhaGrafico(String rotulo, int quantidade, int total, double valor) {
        double pct = percentual(quantidade, total);
        System.out.printf("%s: %3d - %5.1f%% |%s| %s%n",
                rotulo, quantidade, pct, montarBarra(pct), formatarMoeda(valor));
    }

    /**
     * Monta a barra do gráfico com "=" proporcional ao percentual e "-"
     * completando o restante. A barra tem 20 caracteres (100%).
     */
    static String montarBarra(double pct) {
        int cheios = (int) Math.round(pct / 100.0 * 20);
        String barra = "";
        for (int i = 0; i < 20; i++) {
            if (i < cheios) {
                barra = barra + "=";
            } else {
                barra = barra + "-";
            }
        }
        return barra;
    }

    /* ==================================================================
     * Opção 7 - Salvar dados
     * ================================================================== */

    /**
     * Salva as reservas no formato "Assento,Sexo,Idade" (mesmo formato
     * lido pela opção 1).
     */
    static void salvarDados(Scanner teclado, int[][] idades, char[][] sexos) {
        System.out.print("Nome do arquivo: ");
        String nomeArquivo = limparNomeArquivo(teclado.nextLine());
        if (nomeArquivo.isEmpty()) {
            System.out.println("Nome de arquivo vazio. Nada foi salvo.");
            return;
        }

        try {
            // Abre o arquivo para escrita (false = sobrescreve, não faz append)
            FileWriter writer = new FileWriter(nomeArquivo, false);
            BufferedWriter buffer = new BufferedWriter(writer);

            // Cabeçalho do CSV
            buffer.write("Assento,Sexo,Idade");
            buffer.newLine();

            // Uma linha por assento reservado, ex.: "A2,F,12"
            int salvos = 0;
            for (int l = 0; l < idades.length; l++) {
                for (int c = 0; c < idades[0].length; c++) {
                    if (estaReservado(idades, l, c)) {
                        String registro = String.format("%s,%c,%d", nomeAssento(l, c), sexos[l][c], idades[l][c]);
                        buffer.write(registro);
                        buffer.newLine();
                        salvos++;
                    }
                }
            }

            buffer.close(); // descarrega o buffer no arquivo e fecha
            System.out.println(salvos + " reserva(s) salva(s) em " + new File(nomeArquivo).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    /* ==================================================================
     * Opção 8 - Integrantes
     * ================================================================== */

    /**
     * Mostra o nome dos integrantes do grupo.
     */
    static void mostrarIntegrantes() {
        // Nomes dos integrantes do grupo (editar antes da entrega)
        String[] integrantes = {
            "Maria Clara Siqueira",
            "Integrante 2",
            "Integrante 3",
        };

        System.out.println("Integrantes do grupo:");
        for (int i = 0; i < integrantes.length; i++) {
            System.out.println("  " + (i + 1) + ". " + integrantes[i]);
        }
    }

    /* ==================================================================
     * Funções de manipulação das matrizes
     * ================================================================== */

    /** Marca todos os assentos como livres. */
    static void limparReservas(int[][] idades, char[][] sexos) {
        for (int l = 0; l < idades.length; l++) {
            for (int c = 0; c < idades[0].length; c++) {
                liberar(idades, sexos, l, c);
            }
        }
    }

    /** Grava uma reserva no assento (l, c). */
    static void reservar(int[][] idades, char[][] sexos, int l, int c, char sexo, int idade) {
        sexos[l][c] = sexo;
        idades[l][c] = idade;
    }

    /** Libera o assento (l, c). */
    static void liberar(int[][] idades, char[][] sexos, int l, int c) {
        sexos[l][c] = ' ';
        idades[l][c] = -1; // -1 marca o assento como livre
    }

    /** Indica se o assento (l, c) está reservado (idade diferente de -1). */
    static boolean estaReservado(int[][] idades, int l, int c) {
        return idades[l][c] != -1;
    }

    /** Conta quantos assentos estão reservados. */
    static int contarReservados(int[][] idades) {
        int total = 0;
        for (int l = 0; l < idades.length; l++) {
            for (int c = 0; c < idades[0].length; c++) {
                if (estaReservado(idades, l, c)) {
                    total++;
                }
            }
        }
        return total;
    }

    /* ==================================================================
     * Funções de preço e classificação
     * ================================================================== */

    /** Valor pago de acordo com a idade: meia até 17 anos e a partir de 60. */
    static double calcularValor(int idade, double valorIngresso) {
        if (idade <= 17 || idade >= 60) {
            return valorIngresso / 2;
        }
        return valorIngresso;
    }

    /** Nome da categoria de pagante conforme a idade. */
    static String categoriaIdade(int idade) {
        if (idade <= 17) {
            return "meia - menor";
        } else if (idade >= 60) {
            return "meia - idoso";
        }
        return "inteira";
    }

    /* ==================================================================
     * Funções de conversão e formatação
     * ================================================================== */

    /** Letra da fileira (0 -> 'A', 1 -> 'B', ...). */
    static char letraFileira(int l) {
        return (char) ('A' + l);
    }

    /** Nome do assento (l, c) no formato "F5". */
    static String nomeAssento(int l, int c) {
        return letraFileira(l) + String.valueOf(c + 1);
    }

    /**
     * Converte um texto como "F5" em {linha, coluna} (índices da matriz).
     * Devolve null se o texto for inválido ou estiver fora da sala.
     */
    static int[] converterAssento(String texto, int linhas, int colunas) {
        texto = texto.trim().toUpperCase();
        if (texto.length() < 2) {
            return null;
        }

        int l = texto.charAt(0) - 'A';
        int c;
        try {
            c = Integer.parseInt(texto.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }

        if (l < 0 || l >= linhas || c < 0 || c >= colunas) {
            return null;
        }
        int[] assento = { l, c };
        return assento;
    }

    /**
     * Procura o arquivo na pasta onde o programa está rodando e, se não
     * achar, nas subpastas dela (útil quando a IDE roda o programa a partir
     * da pasta "de cima" do projeto). Devolve null se não encontrar.
     */
    static File procurarArquivo(String nomeArquivo) {
        File arquivo = new File(nomeArquivo);
        if (arquivo.isFile()) {
            return arquivo;
        }
        if (arquivo.isAbsolute()) {
            return null; // caminho completo errado: não adianta procurar
        }

        File[] itens = new File(System.getProperty("user.dir")).listFiles();
        if (itens != null) {
            for (int i = 0; i < itens.length; i++) {
                if (itens[i].isDirectory()) {
                    File candidato = new File(itens[i], nomeArquivo);
                    if (candidato.isFile()) {
                        return candidato;
                    }
                }
            }
        }
        return null;
    }

    /** Remove espaços e aspas do nome de arquivo digitado. */
    static String limparNomeArquivo(String texto) {
        return texto.trim().replace("\"", "").replace("'", "").trim();
    }

    /** Converte "M"/"F" em caractere; devolve ' ' se inválido. */
    static char converterSexo(String texto) {
        texto = texto.trim().toUpperCase();
        if (texto.equals("M") || texto.equals("F")) {
            return texto.charAt(0);
        }
        return ' ';
    }

    /** Descrição por extenso do sexo. */
    static String descricaoSexo(char sexo) {
        if (sexo == 'M') {
            return "Masculino";
        }
        return "Feminino";
    }

    /** Percentual de parte em relação ao total (0 se total for 0). */
    static double percentual(int parte, int total) {
        if (total == 0) {
            return 0;
        }
        return parte * 100.0 / total;
    }

    /** Formata um valor como moeda brasileira: "R$ 90,00". */
    static String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor);
    }

    /* ==================================================================
     * Funções de leitura com validação
     * ================================================================== */

    /** Lê um inteiro entre min e max, repetindo até ser válido. */
    static int lerInteiro(Scanner teclado, String mensagem, int min, int max) {
        while (true) {
            System.out.print(mensagem);
            String entrada = teclado.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem de erro abaixo
            }
            System.out.println("Valor inválido. Digite um número entre " + min + " e " + max + ".");
        }
    }

    /** Lê um número real maior que zero (aceita vírgula ou ponto). */
    static double lerDoublePositivo(Scanner teclado, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = teclado.nextLine().trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(entrada);
                if (valor > 0) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem de erro abaixo
            }
            System.out.println("Valor inválido. Digite um número maior que zero.");
        }
    }

    /** Lê um assento válido (ex.: F5), repetindo até ser válido. */
    static int[] lerAssento(Scanner teclado, String mensagem, int linhas, int colunas) {
        while (true) {
            System.out.print(mensagem);
            int[] assento = converterAssento(teclado.nextLine(), linhas, colunas);
            if (assento != null) {
                return assento;
            }
            System.out.println("Assento inválido. Use letra de A a " + letraFileira(linhas - 1)
                    + " e número de 1 a " + colunas + ".");
        }
    }

    /** Lê o sexo (M ou F), repetindo até ser válido. */
    static char lerSexo(Scanner teclado, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            char sexo = converterSexo(teclado.nextLine());
            if (sexo != ' ') {
                return sexo;
            }
            System.out.println("Sexo inválido. Digite M ou F.");
        }
    }

    /** Lê uma resposta S/N e devolve true para "S". */
    static boolean lerSimNao(Scanner teclado, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String resposta = teclado.nextLine().trim().toUpperCase();
            if (resposta.equals("S")) {
                return true;
            }
            if (resposta.equals("N")) {
                return false;
            }
            System.out.println("Digite S ou N.");
        }
    }
}
