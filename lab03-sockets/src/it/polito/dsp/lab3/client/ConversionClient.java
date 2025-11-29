package it.polito.dsp.lab3.client;

import it.polito.dsp.lab3.common.Protocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConversionClient {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java ConversionClient <originType> <targetType> <imagePath>");
            System.exit(1);
        }

        String origin = args[0].toUpperCase();
        String target = args[1].toUpperCase();
        Path imagePath = Path.of(args[2]);

        try {
            if (!Files.exists(imagePath)) {
                System.err.println("File does not exist: " + imagePath);
                System.exit(2);
            }

            byte[] fileBytes = Files.readAllBytes(imagePath);

            byte[] result = convert(origin, target, fileBytes);

            // Save result next to original, with target extension
            String baseName = stripExtension(imagePath.getFileName().toString());
            Path outPath = imagePath.getParent().resolve(baseName + "." + target.toLowerCase());
            Files.write(outPath, result);
            System.out.println("Saved converted file to: " + outPath.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] convert(String origin, String target, byte[] fileBytes) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", Protocol.PORT), 5_000);
            socket.setSoTimeout(30_000);

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Request
            out.write(origin.getBytes("US-ASCII"), 0, 3);
            out.write(target.getBytes("US-ASCII"), 0, 3);
            out.writeInt(fileBytes.length);
            out.write(fileBytes);
            out.flush();

            // Response
            byte status = in.readByte();
            int len = in.readInt();

            byte[] payload = new byte[len];
            in.readFully(payload);

            if (status != Protocol.STATUS_OK) {
                String errMsg = new String(payload, "US-ASCII");
                throw new IOException("Server error (" + (char) status + "): " + errMsg);
            }

            return payload;
        }
    }

    private static String stripExtension(String name) {
        int idx = name.lastIndexOf('.');
        return (idx == -1) ? name : name.substring(0, idx);
    }
}
