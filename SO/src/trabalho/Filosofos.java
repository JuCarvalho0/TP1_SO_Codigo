package trabalho;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Jantar dos Filosofos.
 * Cada palito e um semaforo binario. Para evitar deadlock, um semaforo "sala" deixa
 * no maximo 4 dos 5 filosofos tentarem pegar palitos ao mesmo tempo, o que quebra
 * a espera circular (sempre sobra um palito livre para algum filosofo).
 */
public class Filosofos {

    static final int N = 5;
    static final int REFEICOES = 6;

    public static boolean executar(boolean verboso) {
        Semaphore[] palito = new Semaphore[N];
        for (int i = 0; i < N; i++) palito[i] = new Semaphore(1);
        Semaphore sala = new Semaphore(N - 1);
        AtomicIntegerArray comendo = new AtomicIntegerArray(N);
        AtomicInteger violacoes = new AtomicInteger();
        AtomicInteger[] comeu = new AtomicInteger[N];
        for (int i = 0; i < N; i++) comeu[i] = new AtomicInteger();

        Thread[] ts = new Thread[N];
        for (int f = 0; f < N; f++) {
            final int id = f;
            ts[f] = new Thread(() -> {
                int dir = id, esq = (id + 1) % N;       // palitos pi e pi+1
                try {
                    for (int k = 0; k < REFEICOES; k++) {
                        if (verboso) System.out.println("Filosofo " + id + " medita");
                        Util.dormir((int) (Math.random() * 10));
                        sala.acquire();
                        palito[dir].acquire();
                        palito[esq].acquire();
                        comendo.set(id, 1);
                        if (comendo.get((id + 1) % N) == 1 || comendo.get((id + N - 1) % N) == 1) violacoes.incrementAndGet();
                        if (verboso) System.out.println("   Filosofo " + id + " COME (refeicao " + (k + 1) + ")");
                        Util.dormir((int) (Math.random() * 10));
                        comeu[id].incrementAndGet();
                        comendo.set(id, 0);
                        palito[esq].release();
                        palito[dir].release();
                        sala.release();
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "Filosofo-" + f);
        }
        for (Thread t : ts) t.start();
        boolean terminou = Util.aguardar(ts, 20000);

        boolean todos = true;
        for (int i = 0; i < N; i++) if (comeu[i].get() != REFEICOES) todos = false;
        boolean ok = terminou && violacoes.get() == 0 && todos;
        if (verboso) {
            System.out.println("---- Validacao ----");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < N; i++) sb.append("f").append(i).append("=").append(comeu[i]).append(" ");
            System.out.println("Refeicoes: " + sb);
            System.out.println("Violacoes (vizinhos comendo juntos): " + violacoes);
            System.out.println("Terminou sem deadlock: " + terminou);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
