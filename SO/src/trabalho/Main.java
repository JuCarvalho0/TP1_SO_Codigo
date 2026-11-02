package trabalho;

/** Uso: java -cp out trabalho.Main [1..7 | todos | testes] */
public class Main {
    public static void main(String[] args) {
        String op = args.length > 0 ? args[0] : "todos";
        switch (op) {
            case "1": ProdutorConsumidorSem.main(args); break;
            case "2": Barbeiro.main(args); break;
            case "3": LeitoresEscritores.main(args); break;
            case "4": Fumantes.main(args); break;
            case "5": Filosofos.main(args); break;
            case "6": Selvagens.main(args); break;
            case "7": TresRobos.main(args); break;
            case "testes": TestesTodos.main(args); break;
            default:
                String[] nomes = {"Produtor/Consumidor", "Barbeiro Dorminhoco", "Leitores e Escritores",
                        "Fumantes", "Filosofos", "Selvagens", "Tres Robos"};
                boolean[] r = {ProdutorConsumidorSem.executar(false), Barbeiro.executar(false),
                        LeitoresEscritores.executar(false), Fumantes.executar(false), Filosofos.executar(false),
                        Selvagens.executar(false), TresRobos.executar(false)};
                for (int i = 0; i < r.length; i++) System.out.println(nomes[i] + ": " + (r[i] ? "OK" : "FALHOU"));
        }
    }
}
