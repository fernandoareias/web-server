package com.fernando;
import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) throws IOException {
        final int PORT = 8080;

        System.out.println("Starting web server...");

        try(var socketServ = new ServerSocket(PORT);){
            Socket socketCli;

            while(true){
                System.out.println( "Servidor Ativo" );

                socketCli = socketServ.accept();
                HttpRequest requisicao = new HttpRequest(socketCli);
                Thread thread = new Thread (requisicao);
                thread.start();
            }
        }
    }
}