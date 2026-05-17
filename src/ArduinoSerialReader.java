import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.nio.charset.StandardCharsets;

public class ArduinoSerialReader {

    public static void main(String[] args) {
        // 1. Elenca le porte seriali disponibili
        SerialPort[] ports = SerialPort.getCommPorts();
        System.out.println("Porte seriali rilevate:");

        if (ports.length == 0) {
            System.out.println("Nessuna porta trovata. Verifica che l'Arduino sia collegato!");
            return;
        }

        for (int i = 0; i < ports.length; i++) {
            System.out.println("[" + i + "] " + ports[i].getSystemPortName() + " - " + ports[i].getPortDescription());
        }

        // 2. Seleziona la porta (In questo esempio prendiamo la prima disponibile, es. COM3 o ttyACM0)
        // Se sai già il nome puoi usare SerialPort.getCommPort("COM3");
        SerialPort arduinoPort = ports[0];
        System.out.println("\nTentativo di connessione a: " + arduinoPort.getSystemPortName());

        // 3. Configura i parametri della seriale (Devono coincidere con Arduino)
        arduinoPort.setBaudRate(115200);
        arduinoPort.setNumDataBits(8);
        arduinoPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        arduinoPort.setParity(SerialPort.NO_PARITY);

        // 4. Apri la porta
        if (arduinoPort.openPort()) {
            System.out.println("Connessione stabilita con successo!");
        } else {
            System.out.println("Impossibile aprire la porta seriale.");
            return;
        }

        // 5. Configura un Listener per leggere i dati riga per riga quando arrivano
        StringBuilder buffer = new StringBuilder();

        arduinoPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }

                // Legge i byte disponibili
                byte[] newData = new byte[arduinoPort.bytesAvailable()];
                int numRead = arduinoPort.readBytes(newData, newData.length);

                // Converte i byte in stringa e li accoda al buffer
                String chunk = new String(newData, 0, numRead, StandardCharsets.UTF_8);
                buffer.append(chunk);

                // Elabora il buffer riga per riga (cercando il carattere di a capo '\n')
                int index;
                while ((index = buffer.indexOf("\n")) != -1) {
                    String line = buffer.substring(0, index).trim();
                    buffer.delete(0, index + 1);

                    if (!line.isEmpty()) {
                        processArduinoData(line);
                    }
                }
            }
        });

        // Il programma principale rimane attivo per ricevere i dati
        // Per chiudere la porta in modo pulito alla chiusura dell'app:
        Runtime.getRuntime().addShutdownHook(new Thread(arduinoPort::closePort));
    }

    // 6. Funzione per fare il "Parsing" dei dati ricevuti da Arduino
    private static void processArduinoData(String data) {
        try {
            // Esempio stringa di Arduino: "Distanza_Filtrata:14.20,Target:15.00"
            if (data.contains("Distanza_Filtrata") && data.contains("Target")) {
                String[] parts = data.split(",");

                double distanza = Double.parseDouble(parts[0].split(":")[1]);
                double target = Double.parseDouble(parts[1].split(":")[1]);

                System.out.printf("LOG [Java] -> Pallina a: %.2f cm | Target: %.2f cm%n", distanza, target);

                // QUI PUOI AGGIUNGERE LA TUA LOGICA avanzata:
                // Es. salvare su database, aggiornare un grafico JavaFX, lanciare allarmi.
            } else {
                // Stampa i messaggi di stato ordinari (es. "STATUS: Palla persa...")
                System.out.println("LOG [Arduino] -> " + data);
            }
        } catch (Exception e) {
            // Ignora righe parziali o corrotte durante l'avvio della seriale
        }
    }
}