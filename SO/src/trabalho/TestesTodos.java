package trabalho;

import java.util.function.BooleanSupplier;

/**
 * Executa cada problema varias vezes (sem prints) e informa quantas execucoes passaram.
 */
public class TestesTodos {

    static final int REPETICOES = 20;

    static boolean testar(String nome, BooleanSupplier prog) {
        int ok = 0;

        for (int i = 0; i < REPETICOES; i++) {
            if (prog.getAsBoolean()) {
                ok++;
            }
        }

        System.out.printf("%-28s %2d/%d execucoes OK%n", nome, ok, REPETICOES);
        return ok == REPETICOES;
    }

    public static void main(String[] args) {
        boolean tudo = true;

        tudo &= testar("Produtor/Consumidor",
                () -> ProdutorConsumidorSem.executar(false));

        tudo &= testar("Barbeiro Dorminhoco",
                () -> Barbeiro.executar(false));

        tudo &= testar("Leitores e Escritores",
                () -> LeitoresEscritores.executar(false));

        tudo &= testar("Fumantes",
                () -> Fumantes.executar(false));

        tudo &= testar("Filosofos",
                () -> Filosofos.executar(false));

        tudo &= testar("Selvagens",
                () -> Selvagens.executar(false));

        tudo &= testar("Tres Robos",
                () -> TresRobos.executar(false));

        System.out.println(
                tudo ? "TODOS OS TESTES PASSARAM" : "HOUVE FALHAS"
        );

        System.exit(tudo ? 0 : 1);
    }
}