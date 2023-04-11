package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ThreadServidor extends Thread
{
	private DatagramSocket ss = null;
	private byte[] buffer;
	private int numeroThreads;
	private int cantClientes;
	private String outputLine;
	private long tamanoArchivo2;
	private InetAddress direccion;
	private String mensaje;
	private int puertoCliente;
	private DatagramPacket peticion;
	private int puertoServidor;
	private CyclicBarrier bar;
	
	
	
	public ThreadServidor(DatagramSocket ss, byte[] buffer, int numeroThreads, int cantClientes, String outputLine,
			long tamanoArchivo2, InetAddress direccion, String mensaje, int puertoCliente, DatagramPacket peticion, int puertoServidor,
			CyclicBarrier bar) 
	{
		this.ss = ss;
		this.buffer = buffer;
		this.numeroThreads = numeroThreads;
		this.cantClientes = cantClientes;
		this.outputLine = outputLine;
		this.tamanoArchivo2 = tamanoArchivo2;
		this.direccion = direccion;
		this.mensaje = mensaje;
		this.puertoCliente = puertoCliente;
		this.peticion = peticion;
		this.puertoServidor = puertoServidor;
		this.bar = bar;
	}

	public void run()
	{	
		
		try {
			ProtocoloServidor.procesar(ss, buffer, numeroThreads, cantClientes, outputLine, tamanoArchivo2, direccion, mensaje, puertoCliente, peticion, puertoServidor, bar);
			Servidor.restNumThreads();
		} catch (IOException | NoSuchAlgorithmException | InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		
	}
}
