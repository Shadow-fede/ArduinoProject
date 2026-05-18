import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.nio.charset.StandardCharsets;

/**
 * Gestisce l'intero ciclo di vita della porta seriale.
 * Gira in background ascoltando i dati inviati da Arduino senza rallentare la grafica.
 */
public class SerialListener {
    private SerialPort arduinoPort;         // Oggetto della libreria jSerialComm
    private BallAndBeamVisualizer visualizer; // Riferimento alla finestra principale per poterla aggiornare
    private StringBuilder buffer = new StringBuilder(); // Buffer di memoria in cui accumulare i byte in arrivo

    /**
     * Riceve la finestra principale per poter interagire con i suoi elementi grafici.
     */
    public SerialListener(BallAndBeamVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    /**
     * Cerca le porte disponibili e apre il flusso di comunicazione.
     */
    public void start() {
        // Scansiona il PC alla ricerca di dispositivi connessi (es. COM3, COM4, ttyACM0)
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("Errore: Nessuna porta seriale trovata. Collega l'Arduino!");
            return;
        }

        // Seleziona la prima porta trovata nell'elenco del PC
        arduinoPort = ports[0];
        // Imposta la velocità di trasmissione. DEVE essere identica al Serial.begin(115200) di Arduino!
        arduinoPort.setBaudRate(115200);

        // Tenta di aprire la porta a livello software
        if (arduinoPort.openPort()) {
            System.out.println("Porta aperta con successo su: " + arduinoPort.getSystemPortName());
        } else {
            System.out.println("Impossibile connettersi alla porta seriale (forse è occupata dall'IDE di Arduino?)");
            return;
        }

        // CONFIGURAZIONE DEL LISTENER ASINCRONO
        // Dice alla libreria di avvisarci automaticamente non appena arrivano nuovi byte da leggere
        arduinoPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                // Sicurezza: se l'evento non è legato all'arrivo di dati, esci
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

                // Alloca un array di byte grande esattamente quanto i dati pronti nella coda del sistema operativo
                byte[] newData = new byte[arduinoPort.bytesAvailable()];
                // Trasferisce i byte reali dalla porta seriale dentro il nostro array 'newData'
                int numRead = arduinoPort.readBytes(newData, newData.length);

                // Converte l'array di byte grezzi in una stringa leggibile usando la codifica standard UTF-8
                String chunk = new String(newData, 0, numRead, StandardCharsets.UTF_8);
                // Incolla questo frammento di testo nel buffer accumulatore
                buffer.append(chunk);

                // GESTIONE DEI PACCHETTI RIGA PER RIGA
                int index;
                String ultimaLineaValida = null;

                // Cerca la presenza del carattere di invio/a capo ("\n") nel testo accumulato
                while ((index = buffer.indexOf("\n")) != -1) {
                    // Estrae la stringa completa dall'inizio fino al carattere "\n" rimuovendo spazi inutili (.trim())
                    String line = buffer.substring(0, index).trim();
                    // Cancella la riga appena letta dal buffer per liberare memoria
                    buffer.delete(0, index + 1);

                    // OTTIMIZZAZIONE ANTI-LAG: Se nel buffer ci sono più righe arretrate,
                    // sovrascrive 'ultimaLineaValida' tenendo solo l'ultimo dato in assoluto (il più recente).
                    if (line.contains("Distanza_Filtrata")) {
                        ultimaLineaValida = line;
                    }
                }

                // Se abbiamo estratto almeno una riga integra e aggiornata, la mandiamo al parsing grafico
                if (ultimaLineaValida != null) {
                    parseData(ultimaLineaValida);
                }
            }
        });

        // SHUTDOWN HOOK: Protezione di sistema. Se chiudi il programma Java improvvisamente,
        // questo blocco intercetta l'evento e forza la chiusura della porta seriale, evitando di lasciare la COM bloccata.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (arduinoPort != null && arduinoPort.isOpen()) {
                arduinoPort.closePort();
                System.out.println("Porta seriale rilasciata e chiusa correttamente.");
            }
        }));
    }

    /**
     * Isola il valore numerico della distanza dal testo ricevuto ed effettua l'aggiornamento grafico.
     * Stringa di esempio attesa: "Distanza_Filtrata:14.20,Target:15.00"
     */
    private void parseData(String line) {
        try {
            if (line.contains("Distanza_Filtrata")) {
                // Divide la stringa in due parti usando la virgola come separatore
                // parts[0] diventerà "Distanza_Filtrata:14.20"
                String[] parts = line.split(",");

                // Divide la prima parte usando i due punti ":" e prende l'elemento [1] (ovvero "14.20")
                String valoreTesto = parts[0].split(":")[1];

                // Converte la stringa di testo in un numero matematico decimale (double)
                double distanza = Double.parseDouble(valoreTesto);

                // PASSAGGIO DATI AL THREAD GRAFICO IN BACKGROUND
                // Aggiorna subito la variabile numerica nel pannello grafico in tempo reale
                visualizer.getPannelloGrafico().setDistanzaCorrente(distanza);

                // Chiede a Swing di ridisegnare i pixel non appena la CPU ha un microsecondo libero (Thread Safe).
                // Evita che la grafica si congeli o vada in crash per via dei troppi dati.
                javax.swing.SwingUtilities.invokeLater(() -> visualizer.getPannelloGrafico().repaint());
            }
        } catch (Exception e) {
            // Se la stringa è corrotta o parziale (es. durante l'avvio), il blocco catch
            // intercetta l'errore e lo ignora, evitando che il programma in Java si blocchi o crashi.
        }
    }
}