package cliente;

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

public class Cliente 
{
	
	private static InetAddress direccionServidor = null;
	private static int PUERTO_SERVIDOR = 5000;
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException
	{	
		System.out.println("Iniciando cliente...\n");
		
		String ipServer = "192.168.172.145";
		
		String log = "";
		
		// Enviar primer mensaje de ACK al servidor
		byte[] buffer = new byte[100];
        direccionServidor = InetAddress.getByName(ipServer);
        DatagramSocket socketUDP = new DatagramSocket();
        String mensaje = "ack";
        buffer = mensaje.getBytes();
        DatagramPacket pregunta = new DatagramPacket(buffer, buffer.length, direccionServidor, PUERTO_SERVIDOR);
        socketUDP.send(pregunta);
        
        
        // Recibo el puerto del servidor
        buffer = new byte[100];
        DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
        socketUDP.receive(peticion);
        String puertoS = getData(peticion.getData());
        System.out.println("Puerto del servidor: " + puertoS);
        PUERTO_SERVIDOR = Integer.parseInt(puertoS);
        
        // Recibo id del cliente
        buffer = new byte[100];
        peticion = new DatagramPacket(buffer, buffer.length);
        socketUDP.receive(peticion);
        String id = getData(peticion.getData());
        System.out.println("ID del cliente: " + id);
        log += "Client id: " + id + "\n";
        
        log += "Port of the server" + puertoS + "\n";
        
        // Recibo cantidad de conexiones
        buffer = new byte[100];
        peticion = new DatagramPacket(buffer, buffer.length);
        socketUDP.receive(peticion);
        String cantConexiones = getData(peticion.getData());
        
        // Enviar segundo mensaje de ACK al servidor
        buffer = new byte[100];
        direccionServidor = InetAddress.getByName(ipServer);
        mensaje = "ack";
        buffer = mensaje.getBytes();
        pregunta = new DatagramPacket(buffer, buffer.length, direccionServidor, PUERTO_SERVIDOR);
        socketUDP.send(pregunta);
        
        
        
        
        // Recibir nombre del archivo del servidor
        buffer = new byte[100];
        peticion = new DatagramPacket(buffer, buffer.length);
        socketUDP.receive(peticion);
        String fileName = getData(peticion.getData());
        
        
        // Recibir tamano del archivo del servidor
        buffer = new byte[300];
        peticion = new DatagramPacket(buffer, buffer.length);
        socketUDP.receive(peticion);
        String tamanoArchivo = getData(peticion.getData());
        
        
        // Recibir archivo del servidor
        int tamanoInt = Integer.parseInt(tamanoArchivo);
        System.out.println("Recibiendo el archivo\n");
        long timeI = System.currentTimeMillis();
        String archivo = readFile(tamanoInt, peticion, buffer, socketUDP);
        int bytesRecibidos = archivo.length();
        log += "Bytes recieved:" + bytesRecibidos + " B \n";
        
        long timeF = System.currentTimeMillis();
		long totalTime = timeI - timeF;
		
		if(bytesRecibidos != Integer.parseInt(tamanoArchivo))
		{
			log += "File recieved uncompleted \n";
			System.out.println("El archivo llego incompleto\n");
			System.out.println("Se recibieron: " + bytesRecibidos + "\n");
			System.out.println("Bytes esperados: " + tamanoArchivo + "\n");
		}
		else
		{
			log += "File recieved succesfully \n";
			System.out.println("Recivido el archivo con exito\n");
			
		}
		
        System.out.println("File name: " + fileName);
		System.out.println("File length: " + tamanoArchivo + "B");
		
		log += "File name: " + fileName + "\n";
		log += "File length: " + tamanoArchivo + "B \n";
		log += "Total time: " + totalTime + " miliseconds \n";
		
		createLog(log, id);
		createFile(archivo, id, cantConexiones);
		
        socketUDP.close();
	}
	
	public static String readFile(int tamanoFile, DatagramPacket peticion, byte[] buffer, DatagramSocket socketUDP) throws IOException
	{
		byte[] bufferFinal = new byte[tamanoFile];
		int tamanoBuffer = 20000;
		buffer = new byte[tamanoBuffer];
		boolean termine = false;
		for(int comienzo = 0; comienzo < tamanoFile && !termine; comienzo += tamanoBuffer)
		{
			peticion = new DatagramPacket(buffer, buffer.length);
			socketUDP.receive(peticion);
			byte[] bTermine = new byte[7];
			System.arraycopy(buffer, 0, bTermine, 0, 7);
			if(!(new String(bTermine).equals("termine")))
			{
				if(tamanoFile - comienzo >= tamanoBuffer)
				{
					buffer = peticion.getData();
					System.arraycopy(buffer, 0, bufferFinal, comienzo, tamanoBuffer);
				}
				else
				{
					int nuevoTamano = tamanoFile - comienzo;
					buffer = new byte[nuevoTamano];
					buffer = peticion.getData();
					System.arraycopy(buffer, 0, bufferFinal, comienzo, nuevoTamano);
					termine = true;
						
				}
			}
			else
			{
				termine = true;
			}
			
			
		}
		return (new String(bufferFinal));
	}
	
	public static int changeBytesRecieved(int newSize)
	{
		return newSize;
	}
	
	/**
	 * 
	 * @param content content that is going to be in the log archive
	 * @throws IOException
	 */
	
	public static void createFile(String content, String id, String cantConexiones) throws IOException
	{   
		try {
			Path path = Paths.get("");
			String directoryName = path.toAbsolutePath().toString();
			String nueva = directoryName.replaceAll("bin", "");
			
            PrintWriter writer = new PrintWriter(nueva + "Data/ArchivosRecibidos/Cliente" + id + "-Prueba" + cantConexiones + ".txt", "UTF-8");
            writer.println(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 
	 * @param content content that is going to be in the log archive
	 * @throws IOException
	 */
	
	public static void createLog(String content, String idCliente) throws IOException
	{
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String h = dtf.format(LocalDateTime.now());
        String h2 = h.replace("/", "-");
        Path path = Paths.get("");
		String directoryName = path.toAbsolutePath().toString();
		try {
			String nueva = directoryName.replaceAll("bin", "");
            PrintWriter writer = new PrintWriter(nueva + "Data/logs/" + h2 + "-client" + idCliente + ".log", "UTF-8");
            writer.println(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static String getData(byte[] data)
	{
		boolean foundNull = false;
		int cant = 0;
		for(int i = 0; i < data.length && !foundNull; i++)
		{
			byte p = data[i];
			if(p != 0)
			{
				cant += 1;
			}
			else
			{
				foundNull = true;
			}
			
		}
		byte[] newByte = new byte[cant];
		for(int j = 0; j < cant; j++)
		{
			newByte[j] = data[j];
		}
		
		return (new String(newByte));
	}
	
}
