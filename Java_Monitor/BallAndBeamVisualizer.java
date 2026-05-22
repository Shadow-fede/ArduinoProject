import javax.swing.*;
import java.awt.*;

/**
 * Classe principale che estende JFrame per creare la finestra dell'applicazione.
 * Funge da "regista" (Controller) coordinando l'interfaccia grafica e la comunicazione seriale.
 */
public class BallAndBeamVisualizer extends JFrame {

    // Riferimento al pannello personalizzato che si occupa dei disegni
    private PannelloGrafico pannelloGrafico;

    /**
     * Costruttore: Configura l'aspetto visivo della finestra di Windows/Mac.
     */
    public BallAndBeamVisualizer() {
        // Imposta il titolo della finestra
        setTitle("Ball and Beam - Control System Monitor");

        // Definisce le dimensioni di avvio (Larghezza, Altezza) in pixel
        setSize(800, 400);

        // Specifica che il programma deve chiudersi del tutto quando clicchi sulla "X" della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Posiziona la finestra esattamente al centro dello schermo dell'utente
        setLocationRelativeTo(null);

        // Inizializza il pannello grafico personalizzato
        pannelloGrafico = new PannelloGrafico();

        // Inserisce il pannello al centro della finestra sfruttando il BorderLayout
        add(pannelloGrafico, BorderLayout.CENTER);

        // Rende la finestra visibile a schermo (di base le finestre Java nascono invisibili)
        setVisible(true);
    }

    /**
     * Metodo Getter pubblico. Consente ad altre classi (come SerialListener)
     * di accedere al pannello grafico per passargli i dati o forzarne il ridisegno.
     */
    public PannelloGrafico getPannelloGrafico() {
        return this.pannelloGrafico;
    }

    /**
     * Il punto di avvio (Entry Point) di tutta l'applicazione Java.
     */
    public static void main(String[] args) {
        // 1. Crea l'interfaccia grafica sul thread principale
        BallAndBeamVisualizer finestra = new BallAndBeamVisualizer();

        // 2. Crea l'ascoltatore della seriale, passandogli il riferimento della finestra appena creata
        SerialListener listener = new SerialListener(finestra);

        // 3. Avvia il thread in background per l'ascolto di Arduino
        listener.start();
    }
}