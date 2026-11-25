import java.io.BufferedReader;
import java.io.BufferedWriter;
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

        int MIN_FILEIRAS = 5;
        int MAX_FILEIRAS = 25;
        // Usa o padrão brasileiro nos números (vírgula decimal: R$ 15,00)
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));

        Scanner teclado = new Scanner(System.in);

        System.out.println("=========================================");
        System.out.println("      SISTEMA DE RESERVAS DE CINEMA      ");
        System.out.println("=========================================");

        // Dados iniciais do cinema (fileiras e assentos entre 5 e 25)
        double valorIngresso = lerDoublePositivo(teclado, "Valor do ingresso (R$): ");
        int linhas = lerInteiro(teclado, "Digite a quantidade de fileiras (5 a 25): ", MIN_FILEIRAS, MAX_FILEIRAS,
                "Quantidade inválida. Digite um número inteiro de 5 a 25.");
        int colunas = lerInteiro(teclado, "Digite a quantidade de assentos por fileira (5 a 25): ", MIN_FILEIRAS, MAX_FILEIRAS,
                "Quantidade inválida. Digite um número inteiro de 5 a 25.");

        // Cria a matriz da sala. Cada posição é um assento e guarda o sexo
        // e a idade do ocupante no formato "sexo,idade" (ex.: "F,12").
        // Assento livre guarda o texto vazio "".
        String[][] assentos = new String[linhas][colunas];
        limparReservas(assentos); // o Java cria a matriz com null; aqui vira "" (livre)

        int opcao;
        do {
            opcao = mostrarMenu(teclado);

            switch (opcao) {
                case 1: carregarDados(teclado, assentos); break;
                case 2: consultarAssento(teclado, assentos, valorIngresso); break;
                case 3: reservarAssentos(teclado, assentos); break;
                case 4: liberarAssentos(teclado, assentos); break;
                case 5: mostrarMapa(assentos); break;
                case 6: menuRelatorios(teclado, assentos, valorIngresso); break;
                case 7: salvarDados(teclado, assentos); break;
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

        return lerInteiro(teclado, "Escolha uma opção: ", 1, 9,
                "Opção inválida. Digite um número de 1 a 9.");
    }

    /**
     * Submenu de relatórios (opção 6).
     */
    static void menuRelatorios(Scanner teclado, String[][] assentos, double valorIngresso) {
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
                case "a": relatorioListagem(assentos, valorIngresso); break;
                case "b": relatorioOcupacao(assentos); break;
                case "c": relatorioPorSexo(assentos); break;
                case "d": relatorioGrafico(assentos, valorIngresso); break;
                case "v": break;
                default: System.out.println("Opção inválida.");
            }
        } while (!opcao.equals("v"));
    }

    /* ==================================================================
     * Opção 1 - Carregar dados
     * ================================================================== */

    /**
     * Carrega as reservas de um arquivo no formato "Assento,Sexo,Idade".
     * Primeiro apaga todas as reservas atuais e depois grava as do arquivo.
     * Linhas inválidas ou fora da sala são ignoradas e informadas ao usuário.
     */
    static void carregarDados(Scanner teclado, String[][] assentos) {
        // Pede o nome do arquivo até o usuário digitar com a extensão
        String nomeArquivo;
        do {
            System.out.print("Digite o nome do arquivo com extensão, sem aspas (ex.: reservas.csv): ");

            nomeArquivo = teclado.nextLine().trim();
            if (!nomeArquivo.contains(".")) {
                System.out.println("Digite o nome do arquivo com a extensão, por exemplo: reservas.csv");
            }
        } while (!nomeArquivo.contains("."));

        int carregados = 0;
        int ignorados = 0;
        int numeroLinha = 0;

        try {
            // Abre o arquivo para leitura usando um buffer.
            // Se o arquivo não existir, vai direto para o catch
            // e as reservas atuais não são apagadas.
            FileReader reader = new FileReader(nomeArquivo);
            BufferedReader buffer = new BufferedReader(reader);

            // 1) Apaga todas as reservas atuais
            limparReservas(assentos);

            // 2) Grava as reservas lidas do arquivo
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

                if (processarLinhaArquivo(linhaArquivo, assentos)) {
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
            System.out.println("Não foi possível ler o arquivo \"" + nomeArquivo
                    + "\". Confira se o nome está certo e se o arquivo está na pasta do programa.");
        }
    }

    /**
     * Interpreta uma linha do arquivo ("A2,F,12") e grava a reserva.
     * Devolve true se a linha era válida.
     */
    static boolean processarLinhaArquivo(String linhaArquivo, String[][] assentos) {
        String[] partes = linhaArquivo.split(",");
        if (partes.length != 3) {
            return false;
        }

        int[] assento = converterAssento(partes[0], assentos.length, assentos[0].length);
        char sexo = converterSexo(partes[1]);
        String textoIdade = partes[2].trim();

        if (assento == null || sexo == ' ' || !ehInteiro(textoIdade)) {
            return false;
        }

        int idade = Integer.parseInt(textoIdade);
        if (idade > 150) {
            return false;
        }

        int linha = assento[0];
        int coluna = assento[1];
        reservar(assentos, linha, coluna, sexo, idade);
        return true;
    }

    /* ==================================================================
     * Opção 2 - Consultar assento
     * ================================================================== */

    /**
     * Mostra se um assento está livre ou reservado e, se reservado,
     * o sexo, a idade e o valor pago pelo ocupante.
     */
    static void consultarAssento(Scanner teclado, String[][] assentos, double valorIngresso) {
        int[] assento = lerAssento(teclado, "Assento (ex.: F5): ", assentos.length, assentos[0].length);
        int linha = assento[0];
        int coluna = assento[1];

        String nome = nomeAssento(linha, coluna);

        if (estaReservado(assentos, linha, coluna)) {
            int idade = obterIdade(assentos, linha, coluna);
            System.out.println("Assento " + nome + ": RESERVADO");
            System.out.println("  Sexo : " + descricaoSexo(obterSexo(assentos, linha, coluna)));
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
    static void reservarAssentos(Scanner teclado, String[][] assentos) {
        int colunas = assentos[0].length;
        int[] inicio = lerAssento(teclado, "Assento inicial (ex.: F5): ", assentos.length, colunas);
        int quantidade = lerInteiro(teclado, "Quantidade de assentos: ", 1, colunas,
                "Quantidade inválida. Cada fileira tem " + colunas + " assentos, digite um número de 1 a " + colunas + ".");
        int linha = inicio[0];
        int colunaInicial = inicio[1];
        int colunaFinal = colunaInicial + quantidade - 1;

        // Verifica se os assentos cabem na fileira
        if (colunaFinal >= colunas) {
            System.out.println("Não existem " + quantidade + " assentos a partir de " + nomeAssento(linha, colunaInicial)
                    + " (a fileira termina em " + nomeAssento(linha, colunas - 1) + "). Nenhuma reserva feita.");
            return;
        }

        // Verifica se todos estão livres (sem sobreposição)
        for (int coluna = colunaInicial; coluna <= colunaFinal; coluna++) {
            if (estaReservado(assentos, linha, coluna)) {
                System.out.println("O assento " + nomeAssento(linha, coluna)
                        + " já está reservado. Nenhuma reserva feita.");
                return;
            }
        }

        // Todos disponíveis: pede os dados de cada ocupante
        for (int coluna = colunaInicial; coluna <= colunaFinal; coluna++) {
            System.out.println("-- Assento " + nomeAssento(linha, coluna) + " --");

            char sexo = lerSexo(teclado, "  Sexo (M/F): ");
            int idade = lerInteiro(teclado, "  Idade: ", 0, 150,
                    "  Idade inválida. Digite a idade em anos, de 0 a 150.");
            reservar(assentos, linha, coluna, sexo, idade);
        }

        System.out.println(quantidade + " assento(s) reservado(s) com sucesso.");
    }

    /* ==================================================================
     * Opção 4 - Liberar n assentos
     * ================================================================== */

    /**
     * Libera n assentos consecutivos na mesma fileira a partir do
     * assento informado.
     */
    static void liberarAssentos(Scanner teclado, String[][] assentos) {
        int colunas = assentos[0].length;
        int[] inicio = lerAssento(teclado, "Assento inicial (ex.: F5): ", assentos.length, colunas);
        int quantidade = lerInteiro(teclado, "Quantidade de assentos: ", 1, colunas,
                "Quantidade inválida. Cada fileira tem " + colunas + " assentos, digite um número de 1 a " + colunas + ".");
        int linha = inicio[0];
        int colunaInicial = inicio[1];
        int colunaFinal = colunaInicial + quantidade - 1;

        if (colunaFinal >= colunas) {
            System.out.println("Não existem " + quantidade + " assentos a partir de " + nomeAssento(linha, colunaInicial)
                    + ". Nenhum assento liberado.");
            return;
        }

        int liberados = 0;
        for (int coluna = colunaInicial; coluna <= colunaFinal; coluna++) {
            if (estaReservado(assentos, linha, coluna)) {
                liberar(assentos, linha, coluna);
                liberados++;
            } else {
                System.out.println("  Assento " + nomeAssento(linha, coluna) + " já estava liberado.");
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
    static void mostrarMapa(String[][] assentos) {
        System.out.println();
        System.out.println("Mapa de ocupação do cinema: (\"X\" = reservado e \"·\" = liberado)");

        // Cabeçalho com o número das colunas
        System.out.print("  ");
        for (int coluna = 0; coluna < assentos[0].length; coluna++) {
            System.out.printf(" %02d", coluna + 1);
        }
        System.out.println();

        // Uma linha por fileira
        for (int linha = 0; linha < assentos.length; linha++) {
            System.out.print(letraFileira(linha) + " ");
            for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                if (estaReservado(assentos, linha, coluna)) {
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
    static void relatorioListagem(String[][] assentos, double valorIngresso) {
        if (contarReservados(assentos) == 0) {
            System.out.println("Nenhuma reserva cadastrada.");
            return;
        }

        String separador = "+---------+-----------+-------+------------+";
        System.out.println(separador);
        System.out.println("| Assento | Sexo      | Idade | Valor      |");
        System.out.println(separador);
        for (int linha = 0; linha < assentos.length; linha++) {
            for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                if (estaReservado(assentos, linha, coluna)) {
                    int idade = obterIdade(assentos, linha, coluna);
                    System.out.printf("| %-7s | %-9s | %5d | %10s |%n",
                            nomeAssento(linha, coluna), descricaoSexo(obterSexo(assentos, linha, coluna)),
                            idade, formatarMoeda(calcularValor(idade, valorIngresso)));
                }
            }
        }
        System.out.println(separador);
    }

    /**
     * b) Total de assentos, divididos entre liberados e reservados.
     */
    static void relatorioOcupacao(String[][] assentos) {
        int total = assentos.length * assentos[0].length;
        int reservados = contarReservados(assentos);
        int liberados = total - reservados;

        System.out.println("Total de assentos : " + total);
        System.out.printf("Liberados         : %d (%.1f%%)%n", liberados, percentual(liberados, total));
        System.out.printf("Reservados        : %d (%.1f%%)%n", reservados, percentual(reservados, total));
    }

    /**
     * c) Quantidade de reservas por sexo.
     */
    static void relatorioPorSexo(String[][] assentos) {
        int masculino = 0;
        int feminino = 0;
        for (int linha = 0; linha < assentos.length; linha++) {
            for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                if (estaReservado(assentos, linha, coluna)) {
                    if (obterSexo(assentos, linha, coluna) == 'M') {
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
    static void relatorioGrafico(String[][] assentos, double valorIngresso) {
        int qtdMenor = 0, qtdInteira = 0, qtdIdoso = 0;
        double valorMenor = 0, valorInteira = 0, valorIdoso = 0;

        // Soma quantidade e valor de cada faixa
        for (int linha = 0; linha < assentos.length; linha++) {
            for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                if (estaReservado(assentos, linha, coluna)) {
                    int idade = obterIdade(assentos, linha, coluna);
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
        double porcentagem = percentual(quantidade, total);
        System.out.printf("%s: %3d - %5.1f%% |%s| %s%n",
                rotulo, quantidade, porcentagem, montarBarra(porcentagem), formatarMoeda(valor));
    }

    /**
     * Monta a barra do gráfico com "=" proporcional ao percentual e "-"
     * completando o restante. A barra tem 20 caracteres (100%).
     */
    static String montarBarra(double porcentagem) {
        int cheios = (int) Math.round(porcentagem / 100.0 * 20);
        String barra = "";
        for (int posicao = 0; posicao < 20; posicao++) {
            if (posicao < cheios) {
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
    static void salvarDados(Scanner teclado, String[][] assentos) {
        // Pede o nome do arquivo até o usuário digitar com a extensão
        String nomeArquivo;
        do {
            System.out.print("Nome do arquivo com extensão, sem aspas (ex.: reservas.csv): ");
            nomeArquivo = teclado.nextLine().trim();
            if (!nomeArquivo.contains(".")) {
                System.out.println("Digite o nome do arquivo com a extensão, por exemplo: reservas.csv");
            }
        } while (!nomeArquivo.contains("."));

        try {
            // Abre o arquivo para escrita (false = sobrescreve, não faz append)
            FileWriter writer = new FileWriter(nomeArquivo, false);
            BufferedWriter buffer = new BufferedWriter(writer);

            // Cabeçalho do CSV
            buffer.write("Assento,Sexo,Idade");
            buffer.newLine();

            // Uma linha por assento reservado: nome do assento + o que está
            // guardado na matriz. Ex.: "A2" + "," + "F,12" = "A2,F,12"
            int salvos = 0;
            for (int linha = 0; linha < assentos.length; linha++) {
                for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                    if (estaReservado(assentos, linha, coluna)) {
                        String registro = nomeAssento(linha, coluna) + "," + assentos[linha][coluna];
                        buffer.write(registro);
                        buffer.newLine();
                        salvos++;
                    }
                }
            }

            buffer.close(); // descarrega o buffer no arquivo e fecha
            System.out.println(salvos + " reserva(s) salva(s) em " + nomeArquivo);
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
        String[] integrantes = {
                "Maria Clara Siqueira",
                "Nicolas renana jablonski",
                "Henry de Lima Coitinho",
        };

        System.out.println("Integrantes do grupo:");
        for (int posicao = 0; posicao < integrantes.length; posicao++) {
            System.out.println("  " + (posicao + 1) + ". " + integrantes[posicao]);
        }
    }

    /* ==================================================================
     * Funções de manipulação da matriz de assentos
     * ================================================================== */

    /** Marca todos os assentos como livres. */
    static void limparReservas(String[][] assentos) {
        for (int linha = 0; linha < assentos.length; linha++) {
            for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                liberar(assentos, linha, coluna);
            }
        }
    }

    /** Grava a reserva no assento, no formato "sexo,idade" (ex.: "F,12"). */
    static void reservar(String[][] assentos, int linha, int coluna, char sexo, int idade) {
        assentos[linha][coluna] = sexo + "," + idade;
    }

    /** Libera o assento (texto vazio = livre). */
    static void liberar(String[][] assentos, int linha, int coluna) {
        assentos[linha][coluna] = "";
    }

    /** Indica se o assento está reservado (texto diferente de vazio). */
    static boolean estaReservado(String[][] assentos, int linha, int coluna) {
        return !assentos[linha][coluna].equals("");
    }

    /** Sexo do ocupante: primeira parte do texto "F,12" -> 'F'. */
    static char obterSexo(String[][] assentos, int linha, int coluna) {
        String[] partes = assentos[linha][coluna].split(",");
        return partes[0].charAt(0);
    }

    /** Idade do ocupante: segunda parte do texto "F,12" -> 12. */
    static int obterIdade(String[][] assentos, int linha, int coluna) {
        String[] partes = assentos[linha][coluna].split(",");
        return Integer.parseInt(partes[1]);
    }

    /** Conta quantos assentos estão reservados. */
    static int contarReservados(String[][] assentos) {
        int total = 0;
        for (int linha = 0; linha < assentos.length; linha++) {
            for (int coluna = 0; coluna < assentos[0].length; coluna++) {
                if (estaReservado(assentos, linha, coluna)) {
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
     * ------------------------------------------------------------------
     * O enunciado pede que o usuário informe o assento como letra da
     * fileira + número do assento (ex.: F5), mas a matriz só tem posições
     * numéricas que começam em 0. Por isso convertemos nos dois sentidos:
     *
     *     usuário "F5"  <->  assentos[5][4]
     *
     * Letra <-> número: no Java cada char é guardado como um número
     * (código ASCII). As letras maiúsculas são sequenciais: 'A' = 65,
     * 'B' = 66, ..., 'F' = 70. Então a "distância" de uma letra até o 'A'
     * é o número da fileira:  'F' - 'A' = 70 - 65 = 5.
     *
     * Número do assento: o usuário conta de 1 em diante e a matriz de 0,
     * por isso subtraímos 1 na entrada (5 -> 4) e somamos 1 na saída.
     * ================================================================== */

    /**
     * Número da fileira -> letra (0 -> 'A', 1 -> 'B', 5 -> 'F', ...).
     * 'A' + 5 dá o número 70; o (char) transforma esse número de volta em letra.
     */
    static char letraFileira(int linha) {
        return (char) ('A' + linha);
    }

    /**
     * Posição da matriz -> nome do assento para mostrar ao usuário.
     * Ex.: linha 5, coluna 4 -> "F5" (soma 1 porque a matriz começa em 0).
     */
    static String nomeAssento(int linha, int coluna) {
        return letraFileira(linha) + String.valueOf(coluna + 1);
    }

    /**
     * Nome do assento digitado pelo usuário -> posição da matriz.
     * Ex.: "F5" -> {5, 4}, porque 'F' - 'A' = 5 e 5 - 1 = 4.
     * Devolve null se o texto for inválido ou estiver fora da sala.
     */
    static int[] converterAssento(String texto, int linhas, int colunas) {
        texto = texto.trim().toUpperCase();
        if (texto.length() < 2) {
            return null;
        }

        // Letra da fileira -> número da linha ('F' - 'A' = 5)
        int linha = texto.charAt(0) - 'A';

        // O resto do texto é o número do assento ("5"); a matriz começa em 0
        String textoColuna = texto.substring(1);
        if (!ehInteiro(textoColuna)) {
            return null;
        }
        int coluna = Integer.parseInt(textoColuna) - 1;

        if (linha < 0 || linha >= linhas || coluna < 0 || coluna >= colunas) {
            return null;
        }
        int[] assento = { linha, coluna };
        return assento;
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

    /** Lê um inteiro entre min e max; se for inválido, mostra mensagemErro e pede de novo. */
    static int lerInteiro(Scanner teclado, String mensagem, int min, int max, String mensagemErro) {
        while (true) {
            System.out.print(mensagem);
            String entrada = teclado.nextLine().trim();

            if (ehInteiro(entrada)) {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            }

            System.out.println(mensagemErro);
        }
    }

    /** Lê um número real maior que zero (aceita vírgula ou ponto). */
    static double lerDoublePositivo(Scanner teclado, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = teclado.nextLine().trim().replace(",", ".");

            if (ehNumeroReal(entrada)) {
                double valor = Double.parseDouble(entrada);
                if (valor > 0) {
                    return valor;
                }
            }

            System.out.println("Valor inválido. Digite um número maior que zero.");
        }
    }

    /**
     * Verifica se o texto é um número inteiro sem sinal (só dígitos).
     * Limita a 9 dígitos para o número caber em um int.
     */
    static boolean ehInteiro(String texto) {
        if (texto.length() == 0 || texto.length() > 9) {
            return false;
        }

        for (int posicao = 0; posicao < texto.length(); posicao++) {
            char caractere = texto.charAt(posicao);
            if (caractere < '0' || caractere > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se o texto é um número real sem sinal, como "30" ou "22.50"
     * (só dígitos e no máximo um ponto).
     */
    static boolean ehNumeroReal(String texto) {
        if (texto.length() == 0 || texto.length() > 12) {
            return false;
        }

        int pontos = 0;
        int digitos = 0;

        for (int posicao = 0; posicao < texto.length(); posicao++) {
            char caractere = texto.charAt(posicao);
            if (caractere == '.') {
                pontos++;
            } else if (caractere >= '0' && caractere <= '9') {
                digitos++;
            } else {
                return false;
            }
        }
        return pontos <= 1 && digitos > 0;
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
}
