package trabalho;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problema dos Fumantes.
 * Semaforos: agente (libera o representante para a proxima rodada) e um semaforo por fumante.
 * Fumante 0 tem tabaco, 1 tem papel, 2 tem fosforos.
 */
public class Fumantes {

    static final String[] NOME = {"tabaco", "papel", "fosforos"};
    static final int RODADAS = 15;

    public static boolean executar(boolean verboso) {
        Semaphore agente = new Semaphore(1);
        Semaphore[] fumante = {new Semaphore(0), new Semaphore(0), new Semaphore(0)};
        boolean[] mesa = new boolean[3];            // ingredientes na mesa (so um dono acessa por vez)
        AtomicInteger[] fumou = {new AtomicInteger(), new AtomicInteger(), new AtomicInteger()};
        AtomicInteger violacoes = new AtomicInteger();

        Thread rep = new Thread(() -> {
            try {
                for (int r = 1; r <= RODADAS; r++) {
                    agente.acquire();               // so repoe quando o fumante terminou
                    int falta = (int) (Math.random() * 3);   // ingrediente que NAO vai para a mesa
                    for (int i = 0; i < 3; i++) mesa[i] = (i != falta);
                    if (verboso) System.out.println("Representante (rodada " + r + ") poe na mesa os ingredientes que faltam ao fumante "
                            + falta + " (com " + NOME[falta] + ")");
                    fumante[falta].release();       // desbloqueia o fumante certo
                }
                agente.acquire();                   // espera o ultimo fumante terminar
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Representante");

        Thread[] ts = new Thread[4];
        ts[0] = rep;
        for (int f = 0; f < 3; f++) {
            final int id = f;
            ts[f + 1] = new Thread(() -> {
                try {
                    while (true) {
                        fumante[id].acquire();      // bloqueado ate ser chamado
                        // pega os dois ingredientes: devem ser exatamente os outros dois
                        for (int i = 0; i < 3; i++) {
                            boolean esperado = (i != id);
                            if (mesa[i] != esperado) violacoes.incrementAndGet();
                            mesa[i] = false;
                        }
                        if (verboso) System.out.println("   Fumante " + id + " (" + NOME[id] + ") faz o cigarro e fuma");
                        Util.dormir((int) (Math.random() * 20));
                        fumou[id].incrementAndGet();
                        agente.release();           // avisa o representante
                    }
                } catch (InterruptedException e) { /* fim */ }
            }, "Fumante-" + f);
        }
        for (Thread t : ts) t.start();
        boolean terminou = Util.aguardar(new Thread[]{rep}, 20000);
        for (int f = 1; f < 4; f++) ts[f].interrupt();

        int total = fumou[0].get() + fumou[1].get() + fumou[2].get();
        boolean ok = terminou && violacoes.get() == 0 && total == RODADAS;
        if (verboso) {
            System.out.println("---- Validacao ----");
            System.out.println("Cigarros fumados: tabaco=" + fumou[0] + ", papel=" + fumou[1] + ", fosforos=" + fumou[2]
                    + " (total " + total + " de " + RODADAS + " rodadas)");
            System.out.println("Violacoes (fumante pegou ingredientes errados): " + violacoes);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
