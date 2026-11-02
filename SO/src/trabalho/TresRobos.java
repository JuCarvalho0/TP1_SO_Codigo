package trabalho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Tres robos: sequencia Bart, Lisa, Maggie, Lisa, Bart, Lisa, Maggie, Lisa, ...
 * Cada robo tem um semaforo; so existe uma "permissao" circulando, entao so um robo
 * se move por vez e nao ha espera ocupada. Lisa se move duas vezes por ciclo.
 */
public class TresRobos {

    static final int CICLOS = 5;

    public static boolean executar(boolean verboso) {
        Semaphore bart = new Semaphore(1);     // Bart comeca
        Semaphore lisa = new Semaphore(0);
        Semaphore maggie = new Semaphore(0);
        List<String> log = Collections.synchronizedList(new ArrayList<>());

        Thread tb = new Thread(() -> {
            try {
                for (int c = 0; c < CICLOS; c++) {
                    bart.acquire();
                    move("Bart", log, verboso);
                    lisa.release();
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Bart");
        Thread tl = new Thread(() -> {
            try {
                for (int c = 0; c < CICLOS; c++) {
                    lisa.acquire();
                    move("Lisa", log, verboso);
                    maggie.release();          // 1o movimento da Lisa: vez da Maggie
                    lisa.acquire();
                    move("Lisa", log, verboso);
                    bart.release();            // 2o movimento da Lisa: volta para o Bart
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Lisa");
        Thread tm = new Thread(() -> {
            try {
                for (int c = 0; c < CICLOS; c++) {
                    maggie.acquire();
                    move("Maggie", log, verboso);
                    lisa.release();
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Maggie");

        Thread[] ts = {tb, tl, tm};
        for (Thread t : ts) t.start();
        boolean terminou = Util.aguardar(ts, 20000);

        String[] padrao = {"Bart", "Lisa", "Maggie", "Lisa"};
        boolean sequenciaOk = log.size() == CICLOS * 4;
        for (int i = 0; sequenciaOk && i < log.size(); i++)
            if (!log.get(i).equals(padrao[i % 4])) sequenciaOk = false;

        boolean ok = terminou && sequenciaOk;
        if (verboso) {
            System.out.println("---- Validacao ----");
            System.out.println("Movimentos: " + log.size() + " (esperado " + CICLOS * 4 + ")");
            System.out.println("Sequencia correta (Bart, Lisa, Maggie, Lisa, ...): " + sequenciaOk);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    static void move(String nome, List<String> log, boolean verboso) {
        log.add(nome);
        if (verboso) System.out.println("move() -> " + nome);
        Util.dormir(2);
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
