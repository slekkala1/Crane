package tempest.services;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by swapnalekkala on 11/7/15.
 */
public class FileIOUtils {

    public static byte[] sendByteArraytoReplicate(String sDFSFileName) {
        byte[] fileByteArray = null;
        try {
            //convert file into array of bytes
            System.out.println("replicate " + sDFSFileName + " at " + Inet4Address.getLocalHost().getHostName().toString());
            FileInputStream fileInputStream = new FileInputStream(new File("/home/lekkala2/" + sDFSFileName));
            if (new File("/home/lekkala2/" + sDFSFileName).exists()) {
                System.out.println("File exists " + sDFSFileName + " at " + Inet4Address.getLocalHost().getHostName().toString());
            }
            fileByteArray = IOUtils.toByteArray(fileInputStream);
            System.out.println("filebyteArray length" + fileByteArray.length);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileByteArray;
    }

    public static byte[] writeInputStreamToByteArray(InputStream in, int fileLength) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        if (in != null) {
            int bytesRead, totalBytesRead = 0;
            try {
                int offset;
                do {
                    if (totalBytesRead + 4096 > fileLength) {
                        offset = fileLength - totalBytesRead;
                    } else {
                        offset = 4096;
                    }
                    bytesRead = in.read(buf, 0, offset);
                    if (bytesRead < 0) {
                        break;
                    }
                    baos.write(buf, 0, bytesRead);
                    Arrays.fill(buf, (byte) 0);
                    totalBytesRead += bytesRead;
                } while (totalBytesRead < fileLength);
                System.out.println("Input stream to byte array Total bytes read[" + totalBytesRead + "][ " + fileLength + "]");
                baos.flush();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }

    public static void writeFileToDisk(InputStream in, String sDFSfileName, int length) {
        byte[] buf = new byte[4096];
        if (new File(sDFSfileName).exists()) {
            System.out.println("Chunk file already exists " + sDFSfileName + "return without writing");
            return;
        }
        if (in != null) {
            int bytesRead, offset, totalBytesRead = 0;
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(sDFSfileName)));
                do {
                    if (totalBytesRead + 4096 > length) {
                        offset = length - totalBytesRead;
                    } else {
                        offset = 4096;
                    }
                    bytesRead = in.read(buf, 0, offset);

                    if (bytesRead < 0) {
                        break;
                    }
                    bos.write(buf, 0, bytesRead);
                    Arrays.fill(buf, (byte) 0);
                    totalBytesRead += bytesRead;
                } while (totalBytesRead < length);
                System.out.println("File to Disk Total bytes read[" + totalBytesRead + "][ " + length + "]");
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendFile(Socket socket, String fileName) {
        try {
            BufferedOutputStream outToServer = new BufferedOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(fileName);
            byte[] fileByteArray = IOUtils.toByteArray(fis);
            outToServer.write(ByteBuffer.allocate(4).putInt(fileByteArray.length).array());
            outToServer.write(fileByteArray, 0, fileByteArray.length);
            outToServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendChunkFromDisk(Socket socket, String chunkName) {

        File myFile = new File("/home/lekkala2/" + chunkName);
        //File myFile = new File("/Users/swapnalekkala/cs425-mp-g3/" + chunkName);

        System.out.println("File at " + "/home/lekkala2/" + chunkName);

        try {
            BufferedOutputStream outToServer = new BufferedOutputStream(socket.getOutputStream());
            if (myFile.exists()) {
                System.out.println("Get chunkName" + chunkName + "from Server" + Inet4Address.getLocalHost().getHostName().toString());
                System.out.println("Get chunkName" + chunkName + "from Server" + Inet4Address.getLocalHost().getHostName().toString());
                FileInputStream fis = new FileInputStream(myFile);
                byte[] fileByteArray = IOUtils.toByteArray(fis);
                outToServer.write(ByteBuffer.allocate(4).putInt(fileByteArray.length).array());
                outToServer.write(fileByteArray, 0, fileByteArray.length);
                outToServer.flush();
            } else {
                outToServer.write(ByteBuffer.allocate(4).putInt(0).array());
                outToServer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendFileChunk(Socket socket, byte[] byteArray) {
        try {
            BufferedOutputStream outToServer = new BufferedOutputStream(socket.getOutputStream());
            outToServer.write(ByteBuffer.allocate(4).putInt(byteArray.length).array());
            outToServer.write(byteArray);
            outToServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
