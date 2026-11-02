package trabalho;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Jantar dos Selvagens.
 * Semaforos: mutex (so um selvagem se serve por vez), vazio (acorda o cozinheiro)
 * e cheio (cozinheiro avisa que encheu o caldeirao).
 */
public class Selvagens {

    static final int PORCOES = 5;      // N
    static final int SELVAGENS = 4;
    static final int REFEICOES = 6;    // cada selvagem come 6 vezes

    public static boolean executar(boolean verboso) {
        Semaphore mutex = new Semaphore(1);
        Semaphore vazio = new Semaphore(0);
        Semaphore cheio = new Semaphore(0);
        int[] porcoes = {PORCOES};                     // caldeirao comeca cheio
        AtomicInteger servidas = new AtomicInteger();
        AtomicInteger recargas = new AtomicInteger();
        AtomicInteger violacoes = new AtomicInteger();

        Thread cozinheiro = new Thread(() -> {
            try {
                while (true) {
                    vazio.acquire();                   // dorme ate ser acordado
                    if (porcoes[0] != 0) violacoes.incrementAndGet();   // so enche se estiver vazio
                    if (verboso) System.out.println("Cozinheiro acorda e enche o caldeirao");
                    Util.dormir(5);
                    porcoes[0] = PORCOES;
                    recargas.incrementAndGet();
                    cheio.release();
                }
            } catch (InterruptedException e) { /* fim */ }
        }, "Cozinheiro");

        Thread[] ts = new Thread[SELVAGENS];
        for (int s = 0; s < SELVAGENS; s++) {
            final int id = s;
            ts[s] = new Thread(() -> {
                try {
                    for (int k = 0; k < REFEICOES; k++) {
                        mutex.acquire();               // se serve sozinho
                        if (porcoes[0] == 0) {
                            if (verboso) System.out.println("Selvagem " + id + " ve o caldeirao vazio e acorda o cozinheiro");
                            vazio.release();
                            cheio.acquire();           // espera encher
                        }
                        porcoes[0]--;
                        if (porcoes[0] < 0) violacoes.incrementAndGet();
                        servidas.incrementAndGet();
                        if (verboso) System.out.println("Selvagem " + id + " se serve (restam " + porcoes[0] + ")");
                        mutex.release();
                        Util.dormir((int) (Math.random() * 10));   // come (varios podem comer juntos)
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "Selvagem-" + s);
        }
        cozinheiro.start();
        for (Thread t : ts) t.start();
        boolean terminou = Util.aguardar(ts, 20000);
        cozinheiro.interrupt();

        int total = SELVAGENS * REFEICOES;
        int recargasEsperadas = (total - 1) / PORCOES;  // caldeirao comeca cheio
        boolean ok = terminou && violacoes.get() == 0 && servidas.get() == total
                && recargas.get() == recargasEsperadas;
        if (verboso) {
            System.out.println("---- Validacao ----");
            System.out.println("Porcoes servidas: " + servidas + " de " + total);
            System.out.println("Recargas: " + recargas + " (esperado " + recargasEsperadas + ")");
            System.out.println("Violacoes (porcao negativa / encheu sem estar vazio): " + violacoes);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
