package Client;



import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by rafid on 25/9/2017.
 */
class SendFileClient {


    private File file;
    private String fileID;
    private int chunkSize;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;

    SendFileClient(File file, String fileID, int chunkSize, ObjectInputStream ois,
                          ObjectOutputStream oos, Socket socket) {
        this.file = file;
        this.fileID = fileID;
        this.chunkSize = chunkSize;
        objectInputStream = ois;
        objectOutputStream = oos;
        this.socket = socket;
    }

    boolean send() {
        try {
            socket.setSoTimeout(30000);
            FileInputStream fileInputStream = new FileInputStream(file);
            long fileSize = file.length();
            long j = fileSize/chunkSize, k;
            for(k = 1; k <= j; ++k) {
                byte[] chunk = new byte[chunkSize];
                fileInputStream.read(chunk);
                objectOutputStream.writeObject("Sending file chunk with File ID " + fileID);
                objectOutputStream.writeObject(chunk);
                System.out.println("Chunk " + k + " of size " + chunkSize + " for File ID: " + fileID);
                String msg = (String)objectInputStream.readObject();
                System.out.println("Server: " + msg);
                if(msg.compareTo("Send next chunk") != 0) return false;
            }
            byte[] chunk = new byte[(int)(fileSize - chunkSize*j)];
            fileInputStream.read(chunk);
            objectOutputStream.writeObject("Sending file chunk with File ID " + fileID);
            objectOutputStream.writeObject(chunk);
            System.out.println("Chunk " + k + " of size " +
                    (fileSize-chunkSize*j) + " for File ID: " + fileID);
            String msg = (String)objectInputStream.readObject();
            System.out.println("Server: " + msg);
            if(msg.compareTo("Send next chunk") != 0) return false;
            objectOutputStream.writeObject("No chunk left. Task complete.");
            msg = (String)objectInputStream.readObject();
            System.out.println("Server: " + msg);
            if(msg.compareTo("File received successfully.") == 0) {
                return true;
            }
            else if(msg.compareTo("File size don't match.") == 0){
                return false;
            }
        } catch (SocketTimeoutException e) {
            try {
                objectOutputStream.writeObject("Time Out. Terminating Transmission.");
                return false;
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
