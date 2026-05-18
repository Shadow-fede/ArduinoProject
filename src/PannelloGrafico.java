import javax.swing.*;
import java.awt.*;

/**
 * Pannello personalizzato (JPanel) dedicato esclusivamente al rendering grafico.
 * Trasforma i dati fisici (centimetri) in elementi geometrici bidimensionali (pixel).
 */
public class PannelloGrafico extends JPanel {

    // Stato corrente della simulazione (posizione reale della pallina)
    private double distanzaCorrente = 15.0; // Valore iniziale al centro (15cm)

    // Costanti geometriche del sistema fisico reale
    private final double targetSetpoint = 15.0; // Centro della barra
    private final double maxBarLength = 30.0;  // Lunghezza totale del righello/barra

    public PannelloGrafico() {
        // Imposta il colore di sfondo iniziale del pannello (un blu notte molto scuro)
        setBackground(new Color(15, 23, 42));
    }

    /**
     * Permette di aggiornare la coordinata della pallina dall'esterno.
     */
    public void setDistanzaCorrente(double distanza) {
        this.distanzaCorrente = distanza;
    }

    /**
     * Il metodo fondamentale di Swing. Viene invocato automaticamente dal sistema operativo
     * ogni volta che lo schermo richiede un aggiornamento (es. dopo un repaint()).
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Cancella lo schermo precedente e prepara il pannello per il nuovo disegno
        super.paintComponent(g);

        // Cast obbligatorio a Graphics2D per sbloccare funzioni di disegno avanzate (spessori, rotazioni)
        Graphics2D g2d = (Graphics2D) g;

        // ATTIVAZIONE ANTI-ALIASING: Smussa i bordi delle linee ed evita l'effetto "pixel sgranati"
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();   // Larghezza dinamica corrente della finestra
        int height = getHeight(); // Altezza dinamica corrente della finestra

        // Sfondo di sicurezza (ridisegnato per evitare glitch grafici durante il ridimensionamento)
        g2d.setColor(new Color(15, 23, 42));
        g2d.fillRect(0, 0, width, height);

        // Coordinate del FULCRO (Il punto centrale di ancoraggio della barra)
        int centroX = width / 2;
        int centroY = height / 2 + 50;
        int lunghezzaBarraPixel = 500; // Quanto deve essere lunga la barra sullo schermo

        // 1. DISEGNO DEL FULCRO FISSO (Un triangolo grigio di supporto)
        g2d.setColor(new Color(51, 65, 85));
        int[] xPoints = {centroX, centroX - 20, centroX + 20};
        int[] yPoints = {centroY, centroY + 40, centroY + 40};
        g2d.fillPolygon(xPoints, yPoints, 3); // Disegna e riempie il triangolo

        // 2. CALCOLO DELL'ANGOLO DI ROTAZIONE VISIVO
        // Calcola l'errore istantaneo. Più la pallina è lontana dal centro, più incliniamo la barra per simulare il PID.
        double errore = targetSetpoint - distanzaCorrente;
        // Trasforma l'errore in radianti (richiesti da Java). Il *1.5 serve ad enfatizzare il movimento visivo.
        double angoloVisuale = Math.toRadians(errore * 1.5);

        // TRASFORMAZIONE DEL SISTEMA DI COORDINATE (Matrice di Rotazione)
        g2d.translate(centroX, centroY); // Sposta temporaneamente il "punto 0,0" degli assi cartesiani sul fulcro
        g2d.rotate(-angoloVisuale);       // Ruota l'intero foglio da disegno dell'angolo stabilito

        // 3. DISEGNO DELLA BARRA RUOTATA
        g2d.setColor(new Color(226, 232, 240)); // Colore bianco sporco/silver
        // Imposta uno spessore della linea di 8 pixel con bordi arrotondati (CAP_ROUND)
        g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Disegna la linea centrata nell'origine (0,0) che ora corrisponde al fulcro
        g2d.drawLine(-lunghezzaBarraPixel / 2, 0, lunghezzaBarraPixel / 2, 0);

        // 4. DISEGNO DEL TARGET (Un pallino rosso fisso al centro a 0,0)
        g2d.setColor(new Color(239, 68, 68));
        g2d.fillOval(-6, -6, 12, 12);

        // 5. DISEGNO DELLA PALLINA DINAMICA
        // Calcola la percentuale di posizione della pallina (valore decimale puro da 0.0 a 1.0)
        double percentualePosizione = distanzaCorrente / maxBarLength;
        // Converte la percentuale nei pixel effettivi della barra (mappa da -250 a +250 pixel)
        int ballX = (int) ((percentualePosizione * lunghezzaBarraPixel) - (lunghezzaBarraPixel / 2));
        int diametroPallina = 24;
        // Posiziona l'altezza (Y) in modo che la pallina poggi esattamente *sopra* lo spessore della barra
        int ballY = -diametroPallina / 2 - 4;

        g2d.setColor(new Color(56, 189, 248)); // Colore Azzurro Neon
        g2d.fillOval(ballX - diametroPallina/2, ballY, diametroPallina, diametroPallina);

        // RESET DELLE TRASFORMAZIONI: Riporta gli assi cartesiani nella posizione originale dritta
        g2d.rotate(angoloVisuale);
        g2d.translate(-centroX, -centroY);

        // 6. DISEGNO DELLE INFORMAZIONI DI TESTO (HUD)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(String.format("Posizione Pallina: %.2f cm", distanzaCorrente), 30, 40);
        g2d.setColor(new Color(239, 68, 68));
        g2d.drawString(String.format("Target: %.2f cm", targetSetpoint), 30, 65);
    }
}