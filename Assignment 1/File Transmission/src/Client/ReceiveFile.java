package Client;

import HybridChunk.HybridChunk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

/**
 * Created by rafid on 1/10/2017.
 */
class ReceiveFile {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private HybridChunk hybridChunk;

    ReceiveFile(ObjectInputStream ois, ObjectOutputStream oos, HybridChunk hybridChunk) {
        this.ois = ois;
        this.oos = oos;
        this.hybridChunk = hybridChunk;
    }
    void receive() {
        while(true) {
            try {
                String msg = (String)ois.readObject();
                System.out.println("Server: " + msg);
                if(msg.compareTo("All chunks sent.") == 0) {
                    hybridChunk.setAllChunksPresent(true);
                    System.out.println("Where do you want to save the file?");
                    Scanner scanner = new Scanner(System.in);
                    String pathName = scanner.nextLine() + hybridChunk.getFileName();
                    FileOutputStream fileOutputStream = new FileOutputStream(pathName, true);
                    for(byte[] chunk : hybridChunk.getChunk()) {
                        fileOutputStream.write(chunk);
                        fileOutputStream.flush();
                    }
                    fileOutputStream.close();
                    return;
                }
                byte[] chunk = (byte[])ois.readObject();
                hybridChunk.getChunk().add(chunk);
                oos.writeObject("Send chunk");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
