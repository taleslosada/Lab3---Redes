package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ProtocoloServidor 
{
	private static long tamanoArchivo;
	
	public static void procesar(DatagramSocket ss, byte[] buffer, int numeroThreads, int cantClientes, String outputLine, 
			long tamanoArchivo2, InetAddress direccion, String mensaje, int puertoCliente, 
			DatagramPacket peticion, int puertoServidor, CyclicBarrier bar) throws IOException, NoSuchAlgorithmException, InterruptedException, BrokenBarrierException
	{
		try {
			ss = new DatagramSocket(puertoServidor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		tamanoArchivo = tamanoArchivo2;
		
		// Enviar puerto del servidor
		buffer = new byte[100];
		mensaje = puertoServidor + "";
		buffer = mensaje.getBytes();
		DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
		ss.send(respuesta);
		
		bar.await();
		
        // Enviar id del cliente
		buffer = new byte[100];
		mensaje = numeroThreads + "";
		buffer = mensaje.getBytes();
        respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
        ss.send(respuesta);
        
        
        // Enviar cantConexiones al cliente
        buffer = new byte[100];
		mensaje = cantClientes + "";
		buffer = mensaje.getBytes();
        respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
        ss.send(respuesta);
        
        // Construir log del servidor
		String log = "";
		log += "ID Client: " + numeroThreads +"\n";
		log += "Port of the client: " + puertoCliente + "\n";
		
		// Recibir segundo mensaje de ack del cliente
		buffer = new byte[100];
		peticion = new DatagramPacket(buffer, buffer.length);
		ss.receive(peticion);
		String inputLine = new String(peticion.getData());
		
		System.out.println("mensaje a procesar: " + inputLine);

		log += "Name of the file: " + Servidor.getNombreArchivo() + ".txt \n";
		log += "Size of the file: " + tamanoArchivo + " B \n";
		
		System.out.println("Sending file to client " + numeroThreads + "...");
		
		// Enviar nombre del archivo al cliente
		String nameFile = Servidor.getNombreArchivo();
		buffer = new byte[100];
		buffer = nameFile.getBytes();
		respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
		ss.send(respuesta);

		// Enviar tamano del archivo al cliente
		String ta = tamanoArchivo + "";
		buffer = new byte[300];
		buffer = ta.getBytes();
		respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
		ss.send(respuesta);
		
		// Enviar archivo al cliente
		long timeI = System.currentTimeMillis();
		sendFile(outputLine, direccion, puertoCliente, respuesta, ss);
		
		long timeF = System.currentTimeMillis();
		long totalTime = timeI - timeF;

		
        
		
		System.out.println("File sended succesfully to client " + numeroThreads + "...");
		log += "File recieved succesfully \n";
		log += "Time to transfer the file: " + totalTime + " miliseconds \n";
        
		Servidor.setLog(log);
	}
	
	public static void sendFile(String file, InetAddress direccion, int puertoCliente, DatagramPacket respuesta, DatagramSocket ss) throws IOException, InterruptedException
	{
		byte[] byteFile = file.getBytes();
		int tamanoBuffer = 20000;
		int tamanoFile = byteFile.length;
		byte[] newByteFile = new byte[tamanoBuffer];
		for(int comienzo = 0; comienzo < tamanoFile; comienzo += tamanoBuffer)
		{
			if(tamanoFile - comienzo >= tamanoBuffer)
			{
				System.arraycopy(byteFile, comienzo, newByteFile, 0, tamanoBuffer);
				respuesta = new DatagramPacket(newByteFile, newByteFile.length, direccion, puertoCliente);
				ss.send(respuesta);
			}
			else
			{
				int nuevoTamano = tamanoFile - comienzo;
				newByteFile = new byte[nuevoTamano];
				System.arraycopy(byteFile, comienzo, newByteFile, 0, nuevoTamano);
				respuesta = new DatagramPacket(newByteFile, newByteFile.length, direccion, puertoCliente);
				ss.send(respuesta);
				
			}
		}
		Thread.sleep(100);
		String mensajeFinal = "termine";
		newByteFile = new byte[7];
		newByteFile = mensajeFinal.getBytes();
		respuesta = new DatagramPacket(newByteFile, newByteFile.length, direccion, puertoCliente);
		ss.send(respuesta);
	}
}
