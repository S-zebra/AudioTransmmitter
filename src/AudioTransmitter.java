import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class AudioTransmitter {
  final static int DEFAULT_UDP_SIZE = 8192;
  final static String DEFAULT_DEST_IP = "192.168.150.37";

  public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    int udpMaxSize = 8192;

    // Ask for input
    Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    System.out.println("Mixers");

    for (int i = 0; i < mixerInfo.length; i++) {
      System.out.println("[" + i + "]: " + mixerInfo[i].toString());
    }
    System.out.print("Which device do you use?> ");
    int selectedIndex = Integer.parseInt(reader.readLine());
    Mixer sourceMixer = AudioSystem.getMixer(mixerInfo[selectedIndex]);

    AudioFormat af = new AudioFormat(Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    TargetDataLine dataLine = null;
    try {
      dataLine = (TargetDataLine) sourceMixer.getLine(new DataLine.Info(TargetDataLine.class, af));
      dataLine.open();
      dataLine.start();
    } catch (LineUnavailableException lue) {
      lue.printStackTrace();
      System.exit(-1);
    }

    // Ask for network

    System.out.print("Destination (IP or device name)?> ");
    String ipAddr = (DEFAULT_DEST_IP.isEmpty() ? reader.readLine() : DEFAULT_DEST_IP);
    System.out.print("Port No.?> ");
    int port = Integer.parseInt(reader.readLine());
    DatagramSocket dgSocket = new DatagramSocket(port);
    int delay = 40; // 1 sec

    while (true) {
      Thread.sleep(delay);
      byte[] rawData = new byte[udpMaxSize];
      dataLine.read(rawData, 0, rawData.length);
      DatagramPacket packet = new DatagramPacket(rawData, rawData.length, InetAddress.getByName(ipAddr), 5000);
      System.out.println(packet.getAddress().getHostAddress() + ":" + packet.getPort());
      try {
        dgSocket.send(packet);

        // System.out.println("sent " + udpMaxSize + "bytes");
      } catch (IOException ioe) {
        // ioe.printStackTrace();
        udpMaxSize -= 4;
        System.out.println("UDP Max Size: " + udpMaxSize);
      }
    }
  }
}
