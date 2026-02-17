package TrainningRoom.HTTP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIFromScratch {
    public static List<User> dbUsers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket server = new ServerSocket(port);

        if (server != null) {
            System.out.println("Servidor rodando na porta " + port + "!");
        }

        while (true) {
            final Socket client = server.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

            String methodFlag = "";
            List<String> routeParams = new ArrayList<>();
            int contentLength = 0;

            while (true) {
                String line = reader.readLine();
                System.out.println("Header : " + line);

                if (line.startsWith("Content-Length")) {
                    String contentLengthLine = line.split(":")[1];
                    contentLength = Integer.parseInt(contentLengthLine.trim());                
                }

                if (line.equals("")) break;
                
                if (line.contains("GET")) {
                    routeParams = getUrlParams(line);
                    methodFlag = "GET";
                } else if (line.contains("POST")) {
                    methodFlag = "POST";
                } else if (line.contains("DELETE")) {
                    routeParams = getUrlParams(line);
                    methodFlag = "DELETE";
                } else if (line.contains("PATCH")) {
                    methodFlag = "PATCH";
                    routeParams = getUrlParams(line);
                }

            }

            if (methodFlag == "") {
                sendBadRequestResponse(writer);
                closeAPIResources(writer, reader, client);
                continue;
            }

            String body = "";
            if (contentLength > 0) {
                body = readBody(reader, contentLength);
            }

            handleRequest(methodFlag, routeParams, body, writer);
            closeAPIResources(writer, reader, client);
        }
    }

    private static List<String> getUrlParams(String headerLine) {
        int startingIndex = headerLine.indexOf('/');
        int endingIndex = headerLine.indexOf(" HTTP");
        String paramsString = headerLine.substring(startingIndex + 1, endingIndex);

        String[] params = paramsString.split("/");
        
        return List.of(params);
    }

    private static void closeAPIResources(PrintWriter writer, BufferedReader reader, Socket client) throws IOException {
        writer.flush();
        writer.close();
        reader.close();
        client.close();
    }

    private static String readBody(BufferedReader reader, int contentLength) throws IOException {
        char[] bodyChars = new char[contentLength];
        reader.read(bodyChars, 0, contentLength);

        return new String(bodyChars);
    }

    private static void handleRequest(String methodFlag, List<String> routeParams, String body, PrintWriter writer) {
        switch (methodFlag) {
            case "GET":
                handleGETOperations(routeParams, writer);
                break;
            case "POST":
                handlePOSTOperation(body, writer);
                break;
            case "PATCH":
                handlePATCHOperation(routeParams, body, writer);
                break;
            case "DELETE":
                handleDELETEOperation(routeParams, writer);
                break;
            default:
                break;
        }
    }

    private static void handleGETOperations(List<String> routeParams, PrintWriter writer) {
        if (!routeParams.get(0).equals("users")) {
            sendBadRequestResponse(writer);
            return;
        }

        if (routeParams.size() > 1) {
            String id = routeParams.get(1);
            sendUserDataById(id, writer);
        } else if (routeParams.size() == 1) {
            sendUsersData(writer);
        }
    }
    
    private static void handlePOSTOperation(String body, PrintWriter writer) {
        String clrf = "\r\n";

        try {
            String userId = String.format("%s",dbUsers.size() + 1);
            User newUser = User.parseUserJson(userId, body);
            dbUsers.add(newUser);

            writer.print("HTTP/1.1 " + CodeStatus.OK.getStatusPrint() + clrf);
            writer.print("Content-type: application/json" + clrf);
            writer.println("Content-Length: " + newUser.toString().length() + clrf);
            writer.println(newUser.toString());
        } catch (Exception e) {
            sendBadRequestResponse(writer);
        }
    }

    private static void sendBadRequestResponse(PrintWriter writer) {
        String clrf = "\r\n";
        writer.print("HTTP/1.1 " + CodeStatus.BAD_REQUEST.getStatusPrint() + clrf);
        writer.print("Content-type: application/json" + clrf);
        writer.println("Content-Length: " + 0 + clrf);
    }

    private static void sendUserDataById(String id, PrintWriter writer) {
        String clrf = "\r\n";

        if (!dbUsers.isEmpty()) {
            for (User userIte : dbUsers) {
                if (userIte.getId().equals(id) ) {
                    writer.print("HTTP/1.1 " + CodeStatus.OK.getStatusPrint() + clrf);
                    writer.print("Content-type: application/json" + clrf);
                    writer.println("Content-Length: " + userIte.toString().length() + clrf);
                    writer.println(userIte.toString());
                    return;
                }
            }
        } 

        String noUsersMessage = "{\n message: \"Nenhum usuário encontrado com esse id\"\n}}";

        writer.print("HTTP/1.1 " + CodeStatus.NOT_FOUND.getStatusPrint() + clrf);
        writer.print("Content-type: application/json" + clrf);
        writer.println("Content-Length: " + noUsersMessage.length() + clrf);
        writer.println(noUsersMessage);
    }

    private static void sendUsersData(PrintWriter writer) {
        String allUsersAsString = "[";

        if (!dbUsers.isEmpty()) {
            for (int i = 0; i < dbUsers.size(); i++) {
                if (i == 0) allUsersAsString += "\n";
                else if (i > 0) allUsersAsString += ",\n";

                allUsersAsString += dbUsers.get(i).toString() + "\n";
            }
        }

        allUsersAsString += "]";
        String clrf = "\r\n";

        writer.print("HTTP/1.1 " + CodeStatus.OK.getStatusPrint() + clrf);
        writer.print("Content-type: application/json" + clrf);
        writer.println("Content-Length: " + allUsersAsString.toString().length() + clrf);
        writer.println(allUsersAsString.toString());
    }

    private static void handlePATCHOperation(List<String> routeParams, String body, PrintWriter writer) {
        String clrf = "\r\n";
        String responseMessage = "";

        if (routeParams.size() <= 1) {
            responseMessage = "{\n message: \"ID não fornecido!\"\n}}";

            writer.print("HTTP/1.1 " + CodeStatus.BAD_REQUEST.getStatusPrint() + clrf);
            writer.print("Content-type: application/json" + clrf);
            writer.println("Content-Length: " + responseMessage.length() + clrf);
            writer.println(responseMessage);
            return;
        };

        String id = routeParams.get(1);
        boolean userFound = false;
        int userIndex = 0;
        
        for (int i = 0; i < dbUsers.size(); i++) {
            User userIte = dbUsers.get(i);
            if (userIte.getId().equals(id)) {
                userFound = true;
                userIndex = i;
                break;
            }
        }

        if (userFound == false) {
            responseMessage = "{\n message: \"Não foi encontrado o usuário pelo ID fornecido!\"\n}}";

            writer.print("HTTP/1.1 " + CodeStatus.NOT_FOUND.getStatusPrint() + clrf);
            writer.print("Content-type: application/json" + clrf);
            writer.println("Content-Length: " + responseMessage.length() + clrf);
            writer.println(responseMessage);
            return;
        }

        User updatedUser = User.parseUserJson(id, body);
        dbUsers.get(userIndex).updateUser(updatedUser);

        responseMessage = "{\n message: \"Usuário atualizado com sucesso!\"\n}}";
        writer.print("HTTP/1.1 " + CodeStatus.OK.getStatusPrint() + clrf);
        writer.print("Content-type: application/json" + clrf);
        writer.println("Content-Length: " + responseMessage.length() + clrf);
        writer.println(responseMessage);
    }

    private static void handleDELETEOperation(List<String> routeParams, PrintWriter writer) {
        String clrf = "\r\n";
        String responseMessage = "";
        
        if (routeParams.size() > 1) {
            String id = routeParams.get(1);
            boolean userDeleted = false;
            
            for (int i = 0; i < dbUsers.size(); i++) {
                User userIte = dbUsers.get(i);
                if (userIte.getId().equals(id)) {
                    dbUsers.remove(i);
                    userDeleted = true;
                    break;
                }
            }

            if (userDeleted) {
                responseMessage = "{\n message: \"Usuário deletado com sucesso!\"\n}}";
                writer.print("HTTP/1.1 " + CodeStatus.OK.getStatusPrint() + clrf);
                writer.print("Content-type: application/json" + clrf);
                writer.println("Content-Length: " + responseMessage.length() + clrf);
                writer.println(responseMessage);

                return;
            }
        };

        responseMessage = "{\n message: \"Usuário não encontrado!\"\n}}}";

        writer.print("HTTP/1.1 " + CodeStatus.NOT_FOUND.getStatusPrint() + clrf);
        writer.print("Content-type: application/json" + clrf);
        writer.println("Content-Length: " + responseMessage.length() + clrf);
        writer.println(responseMessage);
    }
}
