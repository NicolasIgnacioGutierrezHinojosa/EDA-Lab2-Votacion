import java.time.LocalTime;
import java.util.*;

public class Electo {

    
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

        public int getId() {
            return id;
        }

        public String getRutVotante() {
            return rutVotante;
        }

        public String getCodigoPartido() {
            return codigoPartido;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "Voto{id=" + id +
                    ", rut='" + rutVotante +
                    "', partido='" + codigoPartido +
                    "', hora='" + timestamp + "'}";
        }

        public byte[] toBytes() {
            byte[] rutBytes = rutVotante.getBytes();
            byte[] partBytes = codigoPartido.getBytes();
            byte[] result = new byte[64];
            System.arraycopy(rutBytes, 0, result, 0, Math.min(32, rutBytes.length));
            System.arraycopy(partBytes, 0, result, 32, Math.min(32, partBytes.length));
            return result;
        }
    }


    static class Candidato {
        private int id;
        private String nombre;
        private String partido; // ej: "A25"
        private Queue<Voto> votosRecibidos = new LinkedList<>();

        public Candidato(int id, String nombre, String partido) {
            this.id = id;
            this.nombre = nombre;
            this.partido = partido;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getPartido() {
            return partido;
        }

        public Queue<Voto> getVotosRecibidos() {
            return votosRecibidos;
        }

        public void agregarVoto(Voto v) {
            votosRecibidos.add(v);
        }

        @Override
        public String toString() {
            return id + ". " + nombre + " (Partido: " + partido + ")";
        }
    }


    static class Votante {
        private String rut;
        private String nombre;
        private boolean yaVoto = false;
        private String codigoVotacion = generarCodigoCorto(6);

        public Votante(String rut, String nombre) {
            this.rut = rut;
            this.nombre = nombre;
        }

        private static String generarCodigoCorto(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            return sb.toString();
        }

        public String getRut() {
            return rut;
        }

        public String getNombre() {
            return nombre;
        }

        public boolean yaVoto() {
            return yaVoto;
        }

        public String getCodigoVotacion() {
            return codigoVotacion;
        }

        public void marcarVotado(String nuevoCodigo) {
            this.yaVoto = true;
            this.codigoVotacion = nuevoCodigo;
        }

        @Override
        public String toString() {
            return nombre + " (RUT: " + rut + ") - Código: " + codigoVotacion + " - Ya votó: " + yaVoto;
        }
    }


    static class UrnaElectoral {
        private List<Candidato> listaCandidatos = new ArrayList<>();
        private Stack<Voto> historialVotos = new Stack<>();
        private Queue<Voto> votosReportados = new LinkedList<>();
        private Map<String, Votante> votantesRegistrados = new HashMap<>();
        private boolean urnasCerradas = false;
        private int idCounter = 1;

        private static String generarCodigoCorto(int length) {
            return Votante.generarCodigoCorto(length);
        }

        public void agregarCandidato(Candidato c) {
            listaCandidatos.add(c);
            System.out.println(" Candidato agregado: " + c);
        }

        public void registrarVotante(Votante v) {
            if (votantesRegistrados.containsKey(v.getRut())) {
                System.out.println(" El votante ya está registrado.");
                return;
            }
            votantesRegistrados.put(v.getRut(), v);
            System.out.println(" Votante registrado: " + v.getNombre() +
                    " - Código de votación: " + v.getCodigoVotacion());
        }

        private boolean verificarVotante(String rut) {
            if (urnasCerradas) {
                System.out.println("️ Las urnas están cerradas.");
                return false;
            }
            Votante v = votantesRegistrados.get(rut);
            if (v == null) {
                System.out.println(" Votante no registrado.");
                return false;
            }
            if (v.yaVoto()) {
                System.out.println(" El votante ya emitió su voto.");
                return false;
            }
            return true;
        }

        private Candidato buscarCandidatoPorCodigo(String codigo) {
            for (Candidato c : listaCandidatos) {
                if (c.getPartido().equalsIgnoreCase(codigo)) return c;
            }
            return null;
        }

        public boolean registrarVoto(String rutVotante, String codigoPartido) {
            if (LocalTime.now().isAfter(LocalTime.of(18, 0))) {
                cerrarUrnas();
                return false;
            }
            if (!verificarVotante(rutVotante)) return false;

            Candidato candidato = buscarCandidatoPorCodigo(codigoPartido);
            if (candidato == null) {
                System.out.println(" Código de partido no válido.");
                return false;
            }

            Voto voto = new Voto(idCounter++, rutVotante, codigoPartido);
            candidato.agregarVoto(voto);
            historialVotos.push(voto);

            Votante votante = votantesRegistrados.get(rutVotante);
            String nuevoCodigo = generarCodigoCorto(6);
            votante.marcarVotado(nuevoCodigo);

            System.out.println(" Voto registrado. Tu nuevo código: " + nuevoCodigo);
            return true;
        }

        public void cerrarUrnas() {
            urnasCerradas = true;
            System.out.println(" Urnas cerradas.");
            mostrarVotantesQueNoVotaron();
            mostrarMultasPorNoVotar();
        }

        public boolean reportarVoto(String codigoVotacion) {
            for (Candidato c : listaCandidatos) {
                Iterator<Voto> it = c.getVotosRecibidos().iterator();
                while (it.hasNext()) {
                    Voto v = it.next();
                    Votante vt = votantesRegistrados.get(v.getRutVotante());
                    if (vt != null && vt.getCodigoVotacion().equals(codigoVotacion)) {
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

        public void mostrarMultasPorNoVotar() {
            System.out.println("\n Usuarios reportados por no votar:");
            for (Votante v : votantesRegistrados.values()) {
                if (!v.yaVoto()) {
                    System.out.println("- " + v.getNombre() + " (RUT: " + v.getRut() + ") - Multa asignada");
                }
            }
        }

        public void mostrarVotantesQueNoVotaron() {
            System.out.println("\n Votantes que NO votaron:");
            for (Votante v : votantesRegistrados.values()) {
                if (!v.yaVoto()) {
                    System.out.println("- " + v.getNombre() + " (RUT: " + v.getRut() + ")");
                }
            }
        }

        public void mostrarResultados() {
            System.out.println("\n Resultados:");
            for (Candidato c : listaCandidatos) {
                System.out.println(c.getNombre() + " (" + c.getPartido() + "): " +
                        c.getVotosRecibidos().size() + " voto(s)");
            }
        }

        public void mostrarHistorial() {
            System.out.println("\n Historial de votos (últimos primero):");
            for (Voto v : historialVotos) System.out.println(v);
        }

        public void mostrarVotantesQueVotaron() {
            System.out.println("\n Votantes que ya votaron:");
            for (Votante v : votantesRegistrados.values()) {
                if (v.yaVoto()) {
                    System.out.println("- " + v.getNombre() + " (RUT: " + v.getRut() + ")");
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UrnaElectoral urna = new UrnaElectoral();

        while (true) {
            System.out.println("\n=== Menú ===");
            System.out.println("1. Agregar candidato");
            System.out.println("2. Registrar votante");
            System.out.println("3. Emitir voto");
            System.out.println("4. Reportar voto");
            System.out.println("5. Mostrar resultados");
            System.out.println("6. Mostrar historial de votos");
            System.out.println("7. Mostrar votantes que han votado");
            System.out.println("8. Mostrar votantes que no han votado");
            System.out.println("9. Cerrar urnas y aplicar multas");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            int opcion = sc.nextInt();
            sc.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1:
                    System.out.print("Nombre del candidato: ");
                    String nombre = sc.nextLine();
                    System.out.print("Código del partido (ej: A25): ");
                    String partido = sc.nextLine();
                    urna.agregarCandidato(new Candidato(urna.listaCandidatos.size() + 1, nombre, partido));
                    break;
                case 2:
                    System.out.print("RUT del votante: ");
                    String rut = sc.nextLine();
                    System.out.print("Nombre del votante: ");
                    String nombreVotante = sc.nextLine();
                    urna.registrarVotante(new Votante(rut, nombreVotante));
                    break;
                case 3:
                    System.out.print("RUT del votante: ");
                    String rutV = sc.nextLine();
                    System.out.print("Código del partido: ");
                    String codPartido = sc.nextLine();
                    urna.registrarVoto(rutV, codPartido);
                    break;
                case 4:
                    System.out.print("Código de votación a reportar: ");
                    String cod = sc.nextLine();
                    urna.reportarVoto(cod);
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
                case 0:
                    System.out.println(" Saliendo...");
                    return;
                default:
                    System.out.println(" Opción inválida.");
            }
        }
    }

}
