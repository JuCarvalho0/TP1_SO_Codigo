package trabalho;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problema dos Leitores e Escritores sobre um vetor M.
 * Semaforos: mutex (protege contador rc), db (acesso exclusivo ao vetor) e
 * turno (semaforo justo que impede que leitores causem starvation nos escritores).
 */
public class LeitoresEscritores {

    static final int LEITORES = 5, ESCRITORES = 2, OPERACOES = 15, TAM_M = 4;

    static final int[] M = new int[TAM_M];

    public static boolean executar(boolean verboso) {
        java.util.Arrays.fill(M, 0);
        Semaphore mutex = new Semaphore(1);
        Semaphore db = new Semaphore(1);
        Semaphore turno = new Semaphore(1, true);
        int[] rc = {0};
        AtomicInteger leitoresAtivos = new AtomicInteger();
        AtomicInteger escritoresAtivos = new AtomicInteger();
        AtomicInteger violacoes = new AtomicInteger();
        AtomicInteger leituras = new AtomicInteger(), escritas = new AtomicInteger();
        AtomicInteger maxLeitoresJuntos = new AtomicInteger();

        Thread[] ts = new Thread[LEITORES + ESCRITORES];
        for (int e = 0; e < ESCRITORES; e++) {
            final int id = e;
            ts[e] = new Thread(() -> {
                try {
                    for (int k = 1; k <= OPERACOES; k++) {
                        turno.acquire();
                        db.acquire();                   // acesso exclusivo
                        turno.release();
                        if (escritoresAtivos.incrementAndGet() != 1 || leitoresAtivos.get() != 0) violacoes.incrementAndGet();
                        int valor = id * 100 + k;
                        for (int i = 0; i < TAM_M; i++) {   // escrita nao atomica de proposito
                            M[i] = valor;
                            Util.dormir(1);
                        }
                        escritas.incrementAndGet();
                        if (verboso) System.out.println("Escritor e" + (id + 1) + " gravou M=" + valor + " em todas as posicoes");
                        escritoresAtivos.decrementAndGet();
                        db.release();
                        Util.dormir((int) (Math.random() * 10));
                    }
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }, "Escritor-" + e);
        }
        for (int l = 0; l < LEITORES; l++) {
            final int id = l;
            ts[ESCRITORES + l] = new Thread(() -> {
                try {
                    for (int k = 0; k < OPERACOES; k++) {
                        turno.acquire();                // passa pela catraca
                        turno.release();
                        mutex.acquire();
                        if (++rc[0] == 1) db.acquire(); // primeiro leitor bloqueia escritores
                        mutex.release();

                        int n = leitoresAtivos.incrementAndGet();
                        maxLeitoresJuntos.accumulateAndGet(n, Math::max);
                        if (escritoresAtivos.get() != 0) violacoes.incrementAndGet();
                        int primeiro = M[0];
                        boolean consistente = true;
                        for (int i = 1; i < TAM_M; i++) {
                            Util.dormir(1);
                            if (M[i] != primeiro) consistente = false;   // leitura "rasgada"
                        }
                        if (!consistente) violacoes.incrementAndGet();
                        leituras.incrementAndGet();
                        if (verboso) System.out.println("   Leitor l" + (id + 1) + " leu M inteiro, valor " + primeiro);
                        leitoresAtivos.decrementAndGet();

                        mutex.acquire();
                        if (--rc[0] == 0) db.release(); // ultimo leitor libera escritores
                        mutex.release();
                        Util.dormir((int) (Math.random() * 10));
                    }
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }, "Leitor-" + l);
        }
        for (Thread t : ts) t.start();
        boolean terminou = Util.aguardar(ts, 30000);

        boolean ok = terminou && violacoes.get() == 0
                && escritas.get() == ESCRITORES * OPERACOES
                && leituras.get() == LEITORES * OPERACOES;
        if (verboso) {
            System.out.println("---- Validacao ----");
            System.out.println("Escritas: " + escritas + " | leituras: " + leituras);
            System.out.println("Maximo de leitores ao mesmo tempo: " + maxLeitoresJuntos + " (leituras em paralelo permitidas)");
            System.out.println("Violacoes (escritor junto com outro/leitor, ou leitura inconsistente): " + violacoes);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
