package it.polito.dsp.lab3.server;

import it.polito.dsp.lab3.common.ImageConverter;
import it.polito.dsp.lab3.common.Protocol;

import java.io.*;
import java.net.Socket;

public class Worker implements Runnable {

    private final Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(30_000); // 30s read timeout – avoids deadlocks

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // 1) Read original type (3 ASCII bytes)
            String origin = readAscii(in, 3);
            // 2) Read target type (3 ASCII bytes)
            String target = readAscii(in, 3);
            // 3) Read length (4 bytes, big endian)
            int length = in.readInt();

            System.out.println("Request: " + origin + " -> " + target + " length=" + length);

            // Basic sanity checks
            if (length <= 0 || length > 100 * 1024 * 1024 ||  // 100MB max
                !Protocol.isValidType(origin) ||
                !Protocol.isValidType(target)) {

                sendError(out, Protocol.STATUS_BAD_REQUEST, "Bad request parameters");
                return;
            }

            // 4) Read file bytes
            byte[] data = new byte[length];
            in.readFully(data);

            // 5) Convert
            byte[] converted;
            try {
                converted = ImageConverter.convert(data, origin, target);
            } catch (Exception e) {
                e.printStackTrace();
                sendError(out, Protocol.STATUS_INTERNAL_ERROR, "Conversion error: " + e.getMessage());
                return;
            }

            // 6) Send success response
            out.writeByte(Protocol.STATUS_OK);
            out.writeInt(converted.length);
            out.write(converted);
            out.flush();
            System.out.println("Conversion OK – sent " + converted.length + " bytes");

        } catch (IOException e) {
            System.err.println("Worker I/O error: " + e);
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
            System.out.println("Connection closed");
        }
    }

    private static String readAscii(DataInputStream in, int n) throws IOException {
        byte[] buf = new byte[n];
        in.readFully(buf);
        return new String(buf, "US-ASCII");
    }

    private static void sendError(DataOutputStream out, byte status, String message) throws IOException {
        byte[] msgBytes = message.getBytes("US-ASCII");
        out.writeByte(status);
        out.writeInt(msgBytes.length);
        out.write(msgBytes);
        out.flush();
        System.out.println("Sent error (" + (char) status + "): " + message);
    }
}
