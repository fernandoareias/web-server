package com.fernando;

import java.io.*;
import java.net.*;
import java.util.*;

public final class HttpRequest implements Runnable {

    // Carriage Return + Line Feed
    private static final String CRLF = "\r\n";
    private Socket socket;

    // Senha para autenticação de pasta protegida
    private final String senhaParaAutenticacao = "admin:admin";

    public HttpRequest(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println("Erro ao processar requisição: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processRequest() throws Exception {
        // Configurar entrada e saída
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        // Obter a linha de requisição da mensagem HTTP
        String requestLine = br.readLine();
        System.out.println();
        System.out.println(requestLine);
        logRequest(requestLine, this.socket);

        String headerLine;
        boolean ehAutenticada = false;
        boolean ehRestrito = false;

        StringBuilder log = new StringBuilder(requestLine + System.lineSeparator());

        // Processar cabeçalhos
        while ((headerLine = br.readLine()) != null && headerLine.length() != 0) {
            log.append(headerLine).append(System.lineSeparator());

            if (headerLine.startsWith("Authorization: Basic")) {
                String[] parts = headerLine.split(" ");
                if (parts.length > 2) {
                    String senha = parts[2];
                    if (Base64Coder.decodeString(senha).equals(this.senhaParaAutenticacao)) {
                        ehAutenticada = true;
                    }
                }
            }
            System.out.println(headerLine);
        }

        // Extrair o nome do arquivo da linha de requisição
        StringTokenizer tokenizer = new StringTokenizer(requestLine);
        String metodo = tokenizer.nextToken();
        String arquivo = "." + tokenizer.nextToken();

        boolean existeArquivo = true;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(arquivo);
        } catch (FileNotFoundException e) {
            existeArquivo = false;
        }

        if (arquivo.contains("RESTRITO") && !ehAutenticada) {
            ehRestrito = true;
        }

        String statusLine;
        String contentTypeLine;
        String responseBody = null;

        if (ehRestrito) {
            statusLine = "HTTP/1.0 401 Unauthorized" + CRLF;
            contentTypeLine = "WWW-Authenticate: Basic realm=\"RESTRITO\"" + CRLF;
            responseBody = "<HTML><HEAD><TITLE>Acesso Não Autorizado</TITLE></HEAD><BODY>Acesso Não Autorizado</BODY></HTML>";
            existeArquivo = false;
        } else if (existeArquivo) {
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType(arquivo) + CRLF;
        } else {
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: text/html" + CRLF;
            responseBody = "<HTML><HEAD><TITLE>Arquivo Não Encontrado</TITLE></HEAD><BODY>Arquivo Não Encontrado</BODY></HTML>";
        }

        // Escreve resposta
        dos.writeBytes(statusLine);
        dos.writeBytes(contentTypeLine);
        dos.writeBytes(CRLF);

        if (existeArquivo) {
            sendBytes(fis, dos);
            fis.close();
        } else if (responseBody != null) {
            dos.writeBytes(responseBody);
        }

        dos.close();
        br.close();
        socket.close();
    }

    private void sendBytes(FileInputStream fis, DataOutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int bytes;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String arquivo) {
        if (arquivo.endsWith(".htm") || arquivo.endsWith(".html")) {
            return "text/html";
        }
        if (arquivo.endsWith(".gif")) {
            return "image/gif";
        }
        if (arquivo.endsWith(".jpeg") || arquivo.endsWith(".jpg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private void logRequest(String log, Socket socket) {
        try (FileWriter fw = new FileWriter("log.txt", true)) {
            Date date = new Date();
            String dataRequisicao = date.toString();
            String pulaLinha = System.lineSeparator();

            fw.write("---------------------------------------------" + pulaLinha);
            fw.write("Data de Requisição: " + dataRequisicao + pulaLinha);
            fw.write("Endereço de Origem: " + socket.getLocalSocketAddress() + pulaLinha);
            fw.write("Log: " + log + pulaLinha);
            fw.write("---------------------------------------------" + pulaLinha);
        } catch (IOException e) {
            System.out.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}
