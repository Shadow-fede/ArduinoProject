#include <Servo.h>
#include <UltrasonicHelper.h>

// --- Configurazione Pin ---
const int servoPin = 11;

// --- Parametri di Controllo e Geometria ---
const float maxBarLength = 30.0; // Lunghezza barra in cm
double setpoint = 15.0;          // Centro della barra (Target)
const double deadzone = 0.5;     // Tolleranza di 0.5 cm per evitare vibrazioni

// --- Parametri PID Ottimizzati per SG90 (Più morbidi) ---
double Kp = 0.0;
double Ki = 0.0;
double Kd = 0.0;

// --- Variabili di Stato ---
Servo myServo;
UltrasonicHelper sensore(9,10,5);

double input, output, error, lastError;
double integral, derivative;
unsigned long lastTime;
float lastValidDistance = 15.0;


void setup() {
  Serial.begin(115200); // Velocità alta per l'R4

  sensore.begin();
  myServo.attach(servoPin);
  myServo.write(90); // Mette la barra in piano all'avvio

  lastTime = millis();
}

void loop() {
  unsigned long now = millis();
  double timeChange = (double)(now - lastTime);

  if (timeChange >= 100) { // Campionamento ogni 50 millisecondi
    float rawDistance = sensore.medianDistance();

    // GESTIONE ECCEZIONE: Pallina persa o caduta
    if (rawDistance <= 2 || rawDistance > maxBarLength || rawDistance == -1) {
      myServo.write(90);       // Riporta la barra piatta
      integral = 0;            // Evita l'effetto Windup dell'integrale
      Serial.println("STATUS: Palla persa. Motore in Safe-Mode.");
    }

    // SISTEMA IN FUNZIONE
    else {
      // Filtro complementare molto pesante per stabilizzare l'SG90
      input = (0.90 * lastValidDistance) + (0.10 * rawDistance);
      lastValidDistance = input;

      // Calcolo Errore
      error = setpoint - input;

      // Verifica della Deadzone (per non stressare il micro servo SG90)
      if (abs(error) < deadzone) {
        myServo.write(90); // Forza la barra in pari se l'errore è minimo
        integral = 0;
      } else {
        // Algoritmo PID standard
        integral += error * (timeChange / 1000.0);
        integral = constrain(integral, -10, 10); // Limita l'azione integrale
        derivative = (error - lastError) / (timeChange / 1000.0);

        output = (Kp * error) + (Ki * integral) + (Kd * derivative);

        // Converte l'output in gradi per il servo (limite max di inclinazione: 20 gradi)
        int servoAngle = 90 + output;
        servoAngle = constrain(servoAngle, 60, 120);

        myServo.write(servoAngle);
      }

      // Output Seriale per il Debug grafico (Strumenti -> Serial Plotter)
      Serial.print("Distanza_Filtrata:"); Serial.print(input);
      Serial.print(",");
      Serial.print("Target:"); Serial.println(setpoint);

      lastError = error;
    }
    lastTime = now;
  }
}