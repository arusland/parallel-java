package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the PCDPFilesystem
     *               class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
                    final int ncores) throws IOException {
        ExecutorService service = Executors.newFixedThreadPool(ncores);

        System.out.println(String.format("ncores: %d", ncores));

        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {
            Socket client = socket.accept();

            service.submit(() -> {
                InputStreamReader isr = new InputStreamReader(client.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();

                //System.out.println(String.format("%s - %s", Thread.currentThread().getName(), line));

                String[] parts = line.split(" ");
                String path = parts[1];

                String content = fs.readFile(new PCDPPath(path));

                OutputStream out = client.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out);
                BufferedWriter writer = new BufferedWriter(osw);

                if (content != null) {
                    writer.write("HTTP/1.0 200 OK\r\n");
                    writer.write("\r\n\r\n");
                    writer.write(content);
                } else {
                    writer.write("HTTP/1.0 404 Not Found\r\n");

                    writer.write("\r\n\r\n");
                }
                writer.flush();
                out.close();

                return null;
            });
        }
    }
}
