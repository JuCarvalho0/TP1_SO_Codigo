package trabalho;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problema do Barbeiro Dorminhoco (Tanenbaum).
 * Semaforos: customers (clientes esperando), barbers (barbeiro livre), mutex (protege waiting)
 * e done (fim do corte, para o cliente sair so quando o corte terminar).
 */
public class Barbeiro {

    static final int CADEIRAS = 3;
    static final int CLIENTES = 12;

    public static boolean executar(boolean verboso) {
        Semaphore customers = new Semaphore(0);
        Semaphore barbers = new Semaphore(0);
        Semaphore mutex = new Semaphore(1);
        Semaphore done = new Semaphore(0);
        int[] waiting = {0};                       // regiao critica: protegida por mutex
        AtomicInteger atendidos = new AtomicInteger();
        AtomicInteger desistiram = new AtomicInteger();
        AtomicInteger cortesBarbeiro = new AtomicInteger();
        AtomicInteger emCorte = new AtomicInteger();
        AtomicInteger violacoes = new AtomicInteger();

        Thread barbeiro = new Thread(() -> {
            try {
                while (true) {
                    if (!customers.tryAcquire()) {
                        if (verboso) System.out.println("Barbeiro dorme (sem clientes)");
                        customers.acquire();       // dorme ate chegar cliente
                    }
                    mutex.acquire();
                    waiting[0]--;                  // chama um cliente da sala de espera
                    barbers.release();             // barbeiro pronto
                    mutex.release();
                    if (emCorte.incrementAndGet() > 1) violacoes.incrementAndGet();
                    if (verboso) System.out.println("Barbeiro cortando cabelo");
                    Util.dormir(20);
                    emCorte.decrementAndGet();
                    cortesBarbeiro.incrementAndGet();
                    done.release();                // corte terminado
                }
            } catch (InterruptedException e) { /* fim do expediente */ }
        }, "Barbeiro");

        Thread[] clientes = new Thread[CLIENTES];
        for (int i = 0; i < CLIENTES; i++) {
            final int id = i;
            clientes[i] = new Thread(() -> {
                try {
                    Util.dormir((int) (Math.random() * 120));   // chegada aleatoria
                    mutex.acquire();
                    if (waiting[0] > CADEIRAS || waiting[0] < 0) violacoes.incrementAndGet();
                    if (waiting[0] < CADEIRAS) {
                        waiting[0]++;
                        if (verboso) System.out.println("Cliente " + id + " entra (esperando: " + waiting[0] + ")");
                        customers.release();       // acorda o barbeiro se estiver dormindo
                        mutex.release();
                        barbers.acquire();         // espera o barbeiro
                        done.acquire();            // espera o corte acabar
                        atendidos.incrementAndGet();
                        if (verboso) System.out.println("Cliente " + id + " saiu cortado");
                    } else {
                        mutex.release();           // barbearia cheia: vai embora
                        desistiram.incrementAndGet();
                        if (verboso) System.out.println("Cliente " + id + " foi embora (cheio)");
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "Cliente-" + i);
        }

        barbeiro.start();
        for (Thread t : clientes) t.start();
        boolean terminou = Util.aguardar(clientes, 20000);
        barbeiro.interrupt();
        try { barbeiro.join(1000); } catch (InterruptedException e) { }

        boolean ok = terminou && violacoes.get() == 0
                && atendidos.get() + desistiram.get() == CLIENTES
                && cortesBarbeiro.get() == atendidos.get();
        if (verboso) {
            System.out.println("---- Validacao ----");
            System.out.println("Clientes: " + CLIENTES + " | atendidos: " + atendidos + " | desistiram: " + desistiram);
            System.out.println("Cortes feitos pelo barbeiro: " + cortesBarbeiro);
            System.out.println("Violacoes (2 cortes ao mesmo tempo / fila invalida): " + violacoes);
            System.out.println("RESULTADO: " + (ok ? "OK" : "FALHOU"));
        }
        return ok;
    }

    public static void main(String[] args) {
        System.exit(executar(true) ? 0 : 1);
    }
}
