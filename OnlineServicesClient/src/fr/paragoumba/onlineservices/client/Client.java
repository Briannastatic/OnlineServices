package fr.paragoumba.onlineservices.client;

import fr.paragoumba.onlineservices.api.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        if (args.length < 1){

            System.out.println("Parameters \"hostname\" and \"port\" missing.");
            return;

        }

        String hostname = args[0];

        if (args.length < 2){

            System.out.println("Parameter \"port\" missing.");
            return;

        }

        int port = 1519;

        try {

            port = Integer.parseInt(args[1]);

        } catch (NumberFormatException ignored){

            System.out.println("Error while parsing port, default port 1519 will be used.");

        }

        try(Socket socket = new Socket(hostname, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

            System.out.println("Connected to " + hostname + ":" + port + " !");

            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            String fromServer;
            int status;
            HashMap<String, String> response;
            long start = 0;

            System.out.println("C:" + command);
            out.println(command);

            while ((fromServer = in.readLine()) != null){

                response = parseResponse(fromServer);

                try {

                    status = Integer.parseInt(response.get("status"));

                } catch (NumberFormatException e){

                    status = Status.RESPONSE_CORRUPTED;

                }

                System.out.println("R:" + fromServer);

                if (start != 0 && command.equalsIgnoreCase("ping") && status == Status.OK) System.out.println("Pong ! " + ((double) (System.currentTimeMillis() - start) / 1E3) + "s");
                else if (command.toLowerCase().startsWith("shutdown") && fromServer.equalsIgnoreCase("0") || status == Status.RESPONSE_CORRUPTED){

                    break;
                }

                command = scanner.nextLine();

                System.out.println("C:" + command);

                if (command.equalsIgnoreCase("ping")) start = System.currentTimeMillis();
                else if (command.equalsIgnoreCase("exit")) break;

                out.println(command);

            }

        } catch (IOException e){

            System.out.println("Server isn't connected, try again later. ");

        }
    }

    private static HashMap<String, String> parseResponse(String response){

        response = response.replaceAll("\\{", "").replaceAll("}", "");

        HashMap<String, String> responseArgs = new HashMap<>();
        String[] args = response.split(", ");

        if (args[0].startsWith("status=")) responseArgs.put("status", args[0].substring(7));
        else responseArgs.put("status", String.valueOf(Status.RESPONSE_CORRUPTED));

        if (args.length > 1 && args[1].startsWith("response=")) responseArgs.put("response", args[1].substring(9));

        return responseArgs;

    }
}
