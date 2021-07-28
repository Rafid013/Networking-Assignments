package HybridChunk;

import java.util.ArrayList;

/**
 * Created by rafid on 25/9/2017.
 */
public class HybridChunk {
    private ArrayList<byte[]> chunk;
    private String fileName;
    private boolean allChunksPresent;

    public HybridChunk(){
        chunk = new ArrayList<>();
        allChunksPresent = false;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<byte[]> getChunk() {
        return chunk;
    }

    public void setChunk(ArrayList<byte[]> chunk) {
        this.chunk = chunk;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isAllChunksPresent() {
        return allChunksPresent;
    }

    public void setAllChunksPresent(boolean allChunksPresent) {
        this.allChunksPresent = allChunksPresent;
    }
}
