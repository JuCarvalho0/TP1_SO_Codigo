package trabalho;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Problema do Produtor e Consumidor (Bounded Buffer) com semaforos.
 * Semaforos: vazios (posicoes livres), cheios (posicoes ocupadas), mutex (exclusao mutua).
 */
public class ProdutorConsumidorSem {

    static final int TAM = 5;          // tamanho do buffer
    static final int PRODUTORES = 3;
    static final int CONSUMIDORES = 3;
    static final int ITENS_POR_PRODUTOR = 20;

    static class BufferLimitado {
        private final int[] v = new int[TAM];
        private int in = 0, out = 0, ocupados = 0, maxOcupados = 0;
        final Semaphore vazios = new Semaphore(TAM);
        final Semaphore cheios = new Semaphore(0);
        final Semaphore mutex = new Semaphore(1);
        final AtomicBoolean violacao = new AtomicBoolean(false);

        void put(int x) throws InterruptedException {
            vazios.acquire();          // espera posicao livre
            mutex.acquire();           // entra na regiao critica
            v[in] = x;
            in = (in + 1) % TAM;
            ocupados++;
            if (ocupados > TAM) violacao.set(true);
            maxOcupados = Math.max(maxOcupados, ocupados);
            mutex.release();
            cheios.release();          // sinaliza item disponivel
        }

        int get() throws InterruptedException {
            cheios.acquire();          // espera item
            mutex.acquire();
            int x = v[out];
            out = (out + 1) % TAM;
            ocupados--;
            if (ocupados < 0) violacao.set(true);
            mutex.release();
            vazios.release();          // sinaliza posicao livre
            return x;
        }
    }

    static void dormir(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static boolean executar(boolean verboso) {
        BufferLimitado buf = new BufferLimitado();
        Set<Integer> consumidos = ConcurrentHashMap.newKeySet();
        AtomicBoolean duplicado = new AtomicBoolean(false);
        Thread[] ts = new Thread[PRODUTORES + CONSUMIDORES];
        int total = PRODUTORES * ITENS_POR_PRODUTOR;
        int porConsumidor = total / CONSUMIDORES;

        for (int p = 0; p < PRODUTORES; p++) {
            final int id = p;
            ts[p] = new Thread(() -> {
                try {
                    for (int i = 0; i < ITENS_POR_PRODUTOR; i++) {
                        int item = id * 1000 + i;      // item unico
                        buf.put(item);
                        if (verboso) System.out.println("Produtor " + id + " colocou " + item);
                        dormir((int) (Math.random() * 5));
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "Produtor-" + p);
        }
        for (int c = 0; c < CONSUMIDORES; c++) {
            final int id = c;
            ts[PRODUTORES + c] = new Thread(() -> {
                try {
                    for (int i = 0; i < porConsumidor; i++) {
                        int item = buf.get();
                        if (!consumidos.add(item)) duplicado.set(true);
                        if (verboso) System.out.println("   Consumidor " + id + " retirou " + item);
                        dormir((int) (Math.random() * 5));
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "Consumidor-" + c);
        }
        for (Thread t : ts) t.start();
        boolean terminou = Util.aguardar(ts, 20000);

        boolean ok = terminou && !duplicado.get() && !buf.violacao.get()
                && consumidos.size() == total && buf.ocupados == 0
                && buf.maxOcupados <= TAM;
        if (verboso) {
            System.out.println("---- Validacao ----");
            System.out.println("Itens produzidos: " + total + " | consumidos (distintos): " + consumidos.size());
            System.out.println("Maximo de posicoes ocupadas: " + buf.maxOcupados + " (limite " + TAM + ")");
            System.out.println("Itens duplicados: " + duplicado.get() + " | violacao do buffer: " + buf.violacao.get());
            System.out.println("Terminou sem travar: " + terminou);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
