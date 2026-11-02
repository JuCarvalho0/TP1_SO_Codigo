package trabalho;

/** Funcoes auxiliares compartilhadas pelos programas. */
public class Util {
    /** Espera todas as threads terminarem; devolve false se estourar o tempo (indicio de deadlock). */
    static boolean aguardar(Thread[] ts, long timeoutMs) {
        long fim = System.currentTimeMillis() + timeoutMs;
        for (Thread t : ts) {
            long resto = fim - System.currentTimeMillis();
            try {
                if (resto <= 0) { t.join(1); } else { t.join(resto); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            if (t.isAlive()) {
                for (Thread x : ts) x.interrupt();
                return false;
            }
        }
        return true;
    }

    static void dormir(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
