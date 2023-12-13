package org.example;

import com.esotericsoftware.kryonet.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.request.*;
import org.example.to.response.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

@AllArgsConstructor
@Getter
@Setter
public class App
{



    public static void main( String[] args )
    {
        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }



        Scanner scanner = new Scanner(System.in);

        System.out.println("SELECT START MODE \n 1 - SERVER \n 2 - CLIENT");
        String type = scanner.nextLine();

        if (type.equals("1")){
            System.out.println("START ON " + localhost.getHostAddress());
            startServer();
        }
        else if (type.equals("2")){
            startClient();
        }
        else {
            System.out.println("ERROR");
        }




    }

    private static void startServer() {
        Server server = new Server();
        server.getKryo().register(TextResponseTO.class);
        server.getKryo().register(TextRequestTO.class);

        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof TextRequestTO) {
                    TextRequestTO request = (TextRequestTO) object;

                    // Обработка запроса и отправка ответа
                    TextResponseTO response = new TextResponseTO();
                    response.setText("Received message: " + request.getText());
                    connection.sendTCP(response);
                }
            }
        });

        try {
            server.start();
            server.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient() {
        Client client = new Client();
        client.getKryo().register(TextRequestTO.class);
        client.getKryo().register(TextResponseTO.class);

        Scanner scanner = new Scanner(System.in);
        System.out.println("SELECT IP ADDRESS");
        String ip = scanner.nextLine();

        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof TextResponseTO) {
                    TextResponseTO response = (TextResponseTO) object;
                    System.out.println("Server response: " + response.getText());
                }
            }
        });

        try {
            client.start();
            client.connect(5000, ip, 54555, 54777);

            // Создание и отправка запроса на сервер
            TextRequestTO request = new TextRequestTO();
            request.setText("Hello, server!");
            client.sendTCP(request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.stop();
        }
    }
}
