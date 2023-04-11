package servidor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Servidor 
{
	private final static int PUERTO = 5000;
	private static int numeroThreads = 0;
	private static String nombreArchivo;
	private static String log;
	private static long tamanoArchivo;
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, BrokenBarrierException
	{
		DatagramSocket ss = null;
		boolean continuar = true;
		log = "";
		
		Path path = Paths.get("");
		String directoryName = path.toAbsolutePath().toString();
		System.out.println("Current Working Directory is = " + directoryName);
		
		Scanner leer = new Scanner(System.in);
		
		System.out.println("Servidor iniciado...");
		System.out.println("Ingrese la cantidad de clientes que desea atender en simultaneo: ");
		int cantClientes = leer.nextInt();
		System.out.println("Ingrese el nombre del archivo que desea enviar: ");
		nombreArchivo = leer.next();
		
		CyclicBarrier bar = new CyclicBarrier(cantClientes);
		
		String outputLine = readFile(nombreArchivo);
		
		System.out.println("El servidor termino de leer el archivo \n");
		
		try {
			ss = new DatagramSocket(PUERTO);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int puertoServidor = PUERTO;
		int clientesAceptados = 0;
		byte[] buffer = new byte[100];
		while(continuar)
		{
			puertoServidor++;
			if(clientesAceptados % cantClientes == 0)
			{
				createLog(log);
			}
			//Preparo la respuesta
	        DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
	        //Recibo el datagrama
	        ss.receive(peticion);
	        System.out.println("Recibo la informacion del cliente... \n");
	        int puertoCliente = peticion.getPort();
	        InetAddress direccion = peticion.getAddress();
	        String mensaje = new String(peticion.getData());
	        
			ThreadServidor thread = new ThreadServidor(ss, buffer, numeroThreads, cantClientes, outputLine, tamanoArchivo, direccion, mensaje, puertoCliente, peticion, puertoServidor, bar);
			//ProtocoloServidor.procesar(ss, buffer, numeroThreads, cantClientes, outputLine, tamanoArchivo, direccion, mensaje, puertoCliente, peticion, puertoServidor);
			numeroThreads ++;
			thread.start();
			clientesAceptados ++;
			
			
		}
		ss.close();
		leer.close();
	}
	
	public static synchronized void setLog(String file)
	{
		log += file;
	}
	
	public static synchronized int getNumThreads()
	{
		return numeroThreads;
	}
	
	public static synchronized void restNumThreads()
	{
		numeroThreads --;
	}
	
	public static String getNombreArchivo()
	{
		return nombreArchivo;
	}
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param content content that is going to be in the log archive
	 * @throws IOException
	 */
	
	public static void createLog(String content) throws IOException
	{
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String h = dtf.format(LocalDateTime.now());
        String h2 = h.replace("/", "-");
        Path path = Paths.get("");
		String directoryName = path.toAbsolutePath().toString();
        
		try {
			String nueva = directoryName.replace("bin", "");
            PrintWriter writer = new PrintWriter(nueva + "Data/logs/" + h2 + "-server.log", "UTF-8");
            writer.println(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static String readFile(String ar) throws IOException
	{
		String archive = "";
		
		Path path = Paths.get("");
		String directoryName = path.toAbsolutePath().toString();
		String nueva = directoryName.replace("bin", "");
		File doc = new File(nueva + "Data/" + ar + ".txt");
		
		tamanoArchivo = doc.length();

		BufferedReader obj = new BufferedReader(new FileReader(doc));

		String strng;
		while ((strng = obj.readLine()) != null)
		{
			archive += strng;
		}
			
		
		obj.close();

		return archive;
	}
}
