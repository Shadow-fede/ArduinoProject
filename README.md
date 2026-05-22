# ⚖️ Dynamic Equilibrium: Ball and Beam System

![Arduino](https://img.shields.io/badge/Arduino-00979D?style=for-the-badge&logo=Arduino&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Control Theory](https://img.shields.io/badge/Control_Theory-PID-blue?style=for-the-badge)

A real-time PID (Proportional-Integral-Derivative) control system based on **Arduino Uno R4 WiFi** for balancing a ball on a tilting beam, monitored via a custom **Java Swing** graphical user interface (GUI).

## 📖 Table of Contents
- [About the Project](#-about-the-project)
- [System Architecture](#-system-architecture)
- [Hardware Components](#-hardware-components)
- [Software Requirements](#-software-requirements)
- [Installation and Getting Started](#-installation-and-getting-started)
- [Code Structure](#-code-structure)
- [Future Developments](#-future-developments)

---

## 🎯 About the Project

This project demonstrates the practical application of control systems theory. The goal is to keep a ball in perfect equilibrium (or move it to a specific target position) on a tilting beam.

The system processes position data, calculates the error relative to the setpoint, and drives a servo motor to instantaneously compensate for the ball's movement. All of this happens within a continuous feedback loop sent to a Desktop application for zero-latency telemetric monitoring.

---

## 🧠 System Architecture

1. **Sensor (Input):** The ultrasonic sensor measures the ball's distance. Raw data is cleaned using a software-based **Complementary Filter** to ignore noise spikes.
2. **Controller (Process):** The Arduino Uno calculates the PID algorithm. To prevent vibration (jittering), a deadzone and a limit for the integral action (anti-windup) are implemented.
3. **Actuator (Output):** A micro servo motor adjusts the beam's angle based on the PID output.
4. **Telemetry (Serial):** Data is sent at a 115200 baud rate to the PC, where a multithreaded Java application decodes the packets and updates the UI at 60 FPS.

---

## 🛠 Hardware Components

* **Microcontroller:** Arduino Uno.
* **Distance Sensor:** HC-SR04 (Ultrasonic sensor).
* **Actuator:** Micro Servo SG90 (or MG996R for greater mechanical stability).
* **Structure:** A 30 cm rigid beam, a lightweight ball (e.g., ping pong).

---

## 💻 Software Requirements

To run and modify the project, you will need:
* [Arduino IDE](https://www.arduino.cc/en/software).
* **Java Development Kit (JDK) 11** or higher.
* [jSerialComm](https://fazecast.github.io/jSerialComm/) Java library for asynchronous COM port management.
* [UltrasonicHelper](https://github.com/Shadow-fede/UltrasonicHelper?focus_description=true) Library for the HC-SR04 and HC-SR05 ultrasonic sensors.

---

## 🚀 Installation and Getting Started

### 1. Arduino Setup
1. Open the `ball_and_beam.ino` file in the Arduino IDE.
2. Ensure the hardware connections match the pins declared in the code (`Trig: D9`, `Echo: D10`, `Servo: D11`).
3. Upload the code to the board.

### 2. Java Monitor Setup
1. Open the Java project in your preferred IDE (IntelliJ, Eclipse, VSCode).
2. Ensure you have added the `jSerialComm` `.jar` file to your dependencies (or update the `pom.xml` file if using Maven).
3. Run the `BallAndBeamVisualizer.java` class. 
4. *Note:* Close the Arduino Serial Monitor before starting the Java program, otherwise the port will be busy!

---

## 📂 Code Structure

The repository is divided into two main modules:

```text
📦 Ball-and-Beam-PID
 ┣ 📂 Arduino_Firmware
 ┃ ┗ 📜 ball_and_beam.ino       # C++ source code (PID and Filters)
 ┣ 📂 Java_Monitor
 ┃ ┣ 📜 BallAndBeamVisualizer.java  # Main Controller and JFrame Window
 ┃ ┣ 📜 PannelloGrafico.java        # 2D Rendering Logic (Swing)
 ┃ ┗ 📜 SerialListener.java         # COM port multithreading management
 ┗ 📜 README.md
