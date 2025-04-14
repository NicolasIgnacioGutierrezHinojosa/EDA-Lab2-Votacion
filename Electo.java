//En este codigo ningun animal ha sido maltratado y ningun do while ha sido utilizado//
import java.time.LocalTime;
import java.util.*;

public class Electo {

    // Clase Voto
    static class Voto {
        private int id;
        private String rutVotante;
        private String codigoPartido;
        private String timestamp;

        public Voto(int id, String rutVotante, String codigoPartido) {
            this.id = id;
            this.rutVotante = rutVotante;
            this.codigoPartido = codigoPartido;
            this.timestamp = LocalTime.now().toString();
        }

        public int getId() { return id; }
        public String getRutVotante() { return rutVotante; }
        public String getCodigoPartido() { return codigoPartido; }
        public String getTimestamp() { return timestamp; }

        public String toString() {
            return "Voto{id=" + id + ", rut='" + rutVotante + "', partido='" + codigoPartido + "', hora='" + timestamp + "'}";
        }
    }


    static class Candidato {
        private int id;
        private String nombre;
        private String partido;
        private Queue<Voto> votosRecibidos;

        public Candidato(int id, String nombre, String partido) {
            this.id = id;
            this.nombre = nombre;
            this.partido = partido;
            this.votosRecibidos = new LinkedList<>();
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getPartido() { return partido; }
        public Queue<Voto> getVotosRecibidos() { return votosRecibidos; }

        public void agregarVoto(Voto v) {
            votosRecibidos.add(v);
        }

        public String toString() {
            return id + ". " + nombre + " (Código de Partido: " + partido + ")";
        }
    }


    static class Votante {
        private String rut;
        private String nombre;
        private boolean yaVoto;
        private String codigoVotacion;

        public Votante(String rut, String nombre) {
            this.rut = rut;
            this.nombre = nombre;
            this.yaVoto = false;
            this.codigoVotacion = generarCodigoCorto(6);
        }


        private String generarCodigoCorto(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++){
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }

        public String getRut() { return rut; }
        public String getNombre() { return nombre; }
        public boolean yaVoto() { return yaVoto; }

        public void marcarVotado(String nuevoCodigo) {
            yaVoto = true;
            this.codigoVotacion = nuevoCodigo;
        }
        public String getCodigoVotacion() { return codigoVotacion; }

        public String toString() {
            return nombre + " (RUT: " + rut + ") - Código de votación: " + codigoVotacion;
        }
    }


    static class UrnaElectoral {
        private LinkedList<Candidato> listaCandidatos;
        private Stack<Voto> historialVotos;
        private Queue<Voto> votosReportados;
        private int idCounter;
        private Map<String, Votante> votantesRegistrados;

        private List<Votante> noVotaron;
        private List<Votante> votaron;
        private boolean urnasCerradas;
        private Random random;

        public UrnaElectoral() {
            this.listaCandidatos = new LinkedList<>();
            this.historialVotos = new Stack<>();
            this.votosReportados = new LinkedList<>();
            this.votantesRegistrados = new HashMap<>();
            this.noVotaron = new ArrayList<>();
            this.votaron = new ArrayList<>();
            this.idCounter = 1;
            this.urnasCerradas = false;
            this.random = new Random();
        }


        public void agregarCandidato(Candidato c) {
            listaCandidatos.add(c);
        }


        public void registrarVotante(Votante v) {
            if(votantesRegistrados.containsKey(v.getRut())) {
                System.out.println(" El votante ya está registrado.");
                return;
            }
            votantesRegistrados.put(v.getRut(), v);
            noVotaron.add(v);
            System.out.println(" Votante registrado: " + v.getNombre() + " (RUT: " + v.getRut() + ") - Código de votación: " + v.getCodigoVotacion());
        }


        public boolean verificarVotante(String rut) {
            if (urnasCerradas) {
                System.out.println(" Las urnas están cerradas.");
                return false;
            }
            Votante v = votantesRegistrados.get(rut);
            return v != null && !v.yaVoto();
        }


        private Candidato buscarCandidatoPorCodigo(String codigo) {
            for (Candidato c : listaCandidatos) {
                if (c.getPartido().equalsIgnoreCase(codigo)) {
                    return c;
                }
            }
            return null;
        }


        public boolean registrarVoto(String rutVotante, String codigoPartido) {

            if (LocalTime.now().isAfter(LocalTime.of(22, 0))) {
                urnasCerradas = true;
                System.out.println(" Se han cerrado las urnas por la hora (>= 18:00).");
                return false;
            }
            if (!verificarVotante(rutVotante))
                return false;
            Votante votante = votantesRegistrados.get(rutVotante);
            Candidato candidato = buscarCandidatoPorCodigo(codigoPartido);
            if (candidato == null)
                return false;
            Voto voto = new Voto(idCounter++, rutVotante, codigoPartido);
            candidato.agregarVoto(voto);
            historialVotos.push(voto);

            String nuevoCodigo = generarCodigoCorto(6);
            votante.marcarVotado(nuevoCodigo);

            noVotaron.remove(votante);
            votaron.add(votante);
            System.out.println(" Voto registrado exitosamente. Tu nuevo código de votación es: " + nuevoCodigo);
            return true;
        }


        private String generarCodigoCorto(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++){
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }


        public boolean reportarVoto(String codigoVotacion) {
            for (Candidato c : listaCandidatos) {
                Iterator<Voto> it = c.getVotosRecibidos().iterator();
                while (it.hasNext()) {
                    Voto v = it.next();
                    Votante votante = votantesRegistrados.get(v.getRutVotante());
                    if (votante != null && codigoVotacion.equals(votante.getCodigoVotacion())) {
                        votosReportados.add(v);
                        it.remove();
                        System.out.println(" Voto reportado exitosamente.");
                        return true;
                    }
                }
            }
            System.out.println(" No se encontró un voto con ese código.");
            return false;
        }


        public void mostrarResultados() {
            System.out.println("\n Resultados:");
            for (Candidato c : listaCandidatos) {
                int total = 0;
                for (Voto v : c.getVotosRecibidos()) {
                    if (v.getCodigoPartido().equalsIgnoreCase(c.getPartido())) {
                        total++;
                    }
                }
                System.out.println(c.getNombre() + " (" + c.getPartido() + "): " + total + " voto(s)");
            }
        }
        public Map<String, Integer> obtenerResultados() {
            Map<String, Integer> resultados = new HashMap<>();
            for (Candidato c : listaCandidatos) {
                resultados.put(c.getNombre(), c.getVotosRecibidos().size());
            }
            return resultados;
        }



        public void mostrarHistorial() {
            System.out.println("\n Historial de votos (últimos primero):");
            for (Voto v : historialVotos) {
                System.out.println(v);
            }
        }


        public void mostrarVotantesQueVotaron() {
            System.out.println("\n Votantes que ya votaron:");
            if (votaron.isEmpty()) {
                System.out.println("Nadie ha votado aún.");
                return;
            }
            for (Votante v : votaron) {
                System.out.println(v);
            }
        }


        public void mostrarVotantesQueNoVotaron() {
            System.out.println("\n Votantes que no han votado:");
            if (noVotaron.isEmpty()) {
                System.out.println("Todos han votado.");
                return;
            }
            for (Votante v : noVotaron) {
                System.out.println(v);
            }
        }


        public void cerrarUrnas() {
            urnasCerradas = true;
            System.out.println("\n Urnas cerradas.");
            mostrarVotantesQueNoVotaron();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UrnaElectoral urna = new UrnaElectoral();

        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- MENÚ ELECTO ---");
            System.out.println("1. Agregar candidato");
            System.out.println("2. Registrar votante");
            System.out.println("3. Votar");
            System.out.println("4. Reportar voto");
            System.out.println("5. Ver resultados");
            System.out.println("6. Ver historial de votos");
            System.out.println("7. Ver votantes que ya votaron");
            System.out.println("8. Ver votantes que no han votado");
            System.out.println("9. Cerrar urnas");
            System.out.println("10. Salir");
            System.out.print("Elige una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    System.out.print("Nombre del candidato: ");
                    String nombreCand = scanner.nextLine();
                    System.out.print("Código de partido (ej: A25): ");
                    String codigoPartido = scanner.nextLine();
                    int id = urna.obtenerResultados().size() + 1;
                    urna.agregarCandidato(new Candidato(id, nombreCand, codigoPartido));
                    break;
                case 2:
                    System.out.print("Nombre del votante: ");
                    String nombreVotante = scanner.nextLine();
                    System.out.print("RUT del votante: ");
                    String rut = scanner.nextLine();
                    urna.registrarVotante(new Votante(rut, nombreVotante));
                    break;
                case 3:
                    System.out.print("RUT del votante: ");
                    String rutVoto = scanner.nextLine();
                    System.out.print("Código del partido a votar: ");
                    String codigoVoto = scanner.nextLine();
                    if (!urna.registrarVoto(rutVoto, codigoVoto))
                        System.out.println("Error al registrar el voto (verifica datos o ya votó / urnas cerradas).");
                    break;
                case 4:
                    System.out.print("Código de votación a reportar: ");
                    String codigoReportar = scanner.nextLine();
                    urna.reportarVoto(codigoReportar);
                    break;
                case 5:
                    urna.mostrarResultados();
                    break;
                case 6:
                    urna.mostrarHistorial();
                    break;
                case 7:
                    urna.mostrarVotantesQueVotaron();
                    break;
                case 8:
                    urna.mostrarVotantesQueNoVotaron();
                    break;
                case 9:

                    urna.cerrarUrnas();
                    break;
                case 10:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
        scanner.close();
    }
}


