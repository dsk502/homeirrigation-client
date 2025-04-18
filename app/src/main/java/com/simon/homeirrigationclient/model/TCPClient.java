package com.simon.homeirrigationclient.model;


import android.content.Context;
import android.util.Log;

import com.simon.homeirrigationclient.HICApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class TCPClient {

    public Context context;
    public String serverHost;
    public int serverPort;

    private final int SOCKET_TIMEOUT = 5000;    //The timeout is 5000ms

    //Define error return values
    public final int ERR_NETWORK_TIMEOUT = 1;
    public final int ERR_MESSAGE_FORMAT = 2;   //If the head (is encrypted) or tail (\n) tag of the message is unexpected value, return this number
    public final int ERR_MESSAGE_CONTENT = 3;  //If the content of the message (command or parameters) is unexpected value, return this number
    public final int ERR_INTERRUPTED = 4;
    public final int ERR_OTHERS = 5;



    public TCPClient(Context context, String serverHost, int serverPort) {
        this.context = context;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }


    public int addDeviceRequest(String clientPubkey, DeviceInfo deviceInfo) {
        final int[] result = new int[1];
        Thread addDeviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = addDeviceNetOperations();
            }

            public int addDeviceNetOperations() {
                try (Socket socket = new Socket(serverHost, serverPort)) {
                    //Set the socket timeout
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    String serverPubKey = "";
                    //InputStream is used for reading messages coming from server
                    InputStream in = socket.getInputStream();

                    //OutputStream is used for sending messages to the server
                    OutputStream out = socket.getOutputStream();

                    //Variables for sending and receiving
                    byte[] sendingBytes;
                    String sendingMessage;

                    byte[] receiveBuffer = new byte[1024];
                    int receiveLen;
                    String receivedMessage;
                    String recvCommand;
                    String[] recvParams;

                    //Send add_device(timestamp)
                    sendingMessage = "add_device(" + deviceInfo.clientAddTime + ")";
                    sendingBytes = packMessageNoEncrypt(sendingMessage);
                    out.write(sendingBytes);
                    out.flush();

                    //Receive key_exchange_server(server_pubkey)
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackUnencryptedMessage(receiveBuffer);
                    if(receivedMessage == null) {   //If encrypted or no end tag -> error
                        return ERR_MESSAGE_FORMAT;
                    }

                    //Retrieve command and arguments
                    recvCommand = extractCommand(receivedMessage);
                    recvParams = extractParams(receivedMessage);
                    if(!(recvCommand.equals("key_exchange_server") && recvParams.length == 1)) {
                        return ERR_MESSAGE_CONTENT;
                    }
                    //Store the server_pubkey
                    serverPubKey = new String(recvParams[0]);
                    //Log.println(Log.ERROR, "Server public key:", serverPubKey);


                    //Reply key_exchange_client(client_pubkey)
                    sendingMessage = "key_exchange_client(" + clientPubkey + ")";
                    sendingBytes = packMessageNoEncrypt(sendingMessage);
                    out.write(sendingBytes);
                    out.flush();

                    //Receive and process request_add_param(server_id) -- encrypted
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    Log.println(Log.ERROR, "Received Message:", receivedMessage);
                    recvCommand = extractCommand(receivedMessage);
                    recvParams = extractParams(receivedMessage);
                    if(!(recvCommand.equals("request_add_param") && recvParams.length == 1)) {
                        return ERR_MESSAGE_CONTENT;
                    }
                    String serverId = new String(recvParams[0]);
                    //Log.println(Log.ERROR, "Received", "Received request_add_param");

                    //Reply reply_add_param(mode,water_amount,scheduled_freq,scheduled_time) -- encrypted
                    sendingMessage = "reply_add_param(" + deviceInfo.mode + "," + deviceInfo.waterAmount + "," + deviceInfo.scheduledFreq + "," + deviceInfo.scheduledTime + ")";
                    //Log.println(Log.ERROR, "Server public key:", serverPubKey);

                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubKey);
                    //Log.println(Log.ERROR,"First encrypted message:", new String(sendingBytes));
                    out.write(sendingBytes);
                    out.flush();

                    //Receive finish_add_server() -- encrypted
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    Log.println(Log.ERROR, "Finish add server: ", receivedMessage);
                    recvCommand = extractCommand(receivedMessage);
                    if(!recvCommand.equals("finish_add_server")) {
                        return ERR_MESSAGE_CONTENT;
                    }
                    //Log.println(Log.ERROR, "After finish add server", "Yes");

                    //Update the server id and server public key in deviceInfo object
                    deviceInfo.serverId = serverId;
                    deviceInfo.serverPubkey = serverPubKey;

                    //Save the data to the database
                    HICApplication.getInstance().deviceDatabaseHelper.insertDevice(deviceInfo);

                    //Reply finish_add_client()
                    sendingMessage = "finish_add_client()";
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubKey);
                    Log.println(Log.ERROR, "Finish add client: ", new String(sendingBytes));
                    out.write(sendingBytes);
                    out.flush();

                    //Close the input and output stream
                    in.close();
                    out.close();
                    return 0;

                } catch (SocketTimeoutException e) {
                    return ERR_NETWORK_TIMEOUT;
                } catch (Exception e) {
                    Log.e("TCPClient", "Exception happens: ", e);
                    return ERR_OTHERS;
                }

            }
        });

        addDeviceThread.start();
        try {
            addDeviceThread.join();
        } catch (InterruptedException e) {
            return ERR_INTERRUPTED;
        }
        return result[0];

    }

    public int downloadStatRequest(String serverId, String serverPubKey) {
        final int[] result = new int[1];
        Thread downloadStatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = downloadStatNetOperations();
            }

            private int downloadStatNetOperations() {
                File receiveFile = new File(context.getCacheDir(), "watering_record_" + serverId + "_encrypted.db");
                String outputDatabaseFileName = "watering_record_" + serverId + ".db";
                File outputFile = context.getDatabasePath(outputDatabaseFileName);

                try (
                        Socket socket = new Socket(serverHost, serverPort);
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        FileOutputStream fileOutputStream = new FileOutputStream(receiveFile);
                ) {
                    //Set the socket timeout
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    //For the messages
                    byte[] sendingBytes;
                    String sendingMessage;

                    byte[] receiveBuffer = new byte[1024];
                    int receiveLen;
                    String receivedMessage;
                    String recvCommand;
                    String[] recvParams;

                    //Sending download_stat()
                    sendingMessage = "download_stat()";
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubKey);
                    outputStream.write(sendingBytes);
                    outputStream.flush();

                    //Receive stat_key(aeskey, iv)
                    receiveLen = inputStream.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    recvCommand = extractCommand(receivedMessage);
                    recvParams = extractParams(receivedMessage);
                    if(!(recvCommand.equals("stat_key") && recvParams.length == 2)) {
                        return ERR_MESSAGE_CONTENT;
                    }

                    //Get aes key and iv in base64
                    String aesKeyBase64 = recvParams[0];
                    String ivBase64 = recvParams[1];

                    //Reply stat_key_ok
                    sendingMessage = "stat_key_ok()";
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubKey);
                    outputStream.write(sendingBytes);
                    outputStream.flush();

                    //For the file
                    //Receive file size
                    byte[] fileSizeBytes = new byte[8];
                    int bytesRead = 0;
                    int offset = 0;
                    while (bytesRead < fileSizeBytes.length) {
                        int result = inputStream.read(fileSizeBytes, offset, fileSizeBytes.length - bytesRead);
                        if (result == -1) break;
                        bytesRead += result;
                        offset += result;
                    }

                    long fileSize = 0;
                    for (int i = 0; i < fileSizeBytes.length; i++) {
                        fileSize |= ((long) fileSizeBytes[i] & 0xFFL) << (i * 8);
                    }

                    System.out.println("File size: " + fileSize + " bytes");

                    //Receive file content
                    byte[] buffer = new byte[1024];
                    bytesRead = 0;

                    while (bytesRead < fileSize) {
                        int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesRead));
                        if (read == -1) break;
                        fileOutputStream.write(buffer, 0, read);
                        bytesRead += read;
                    }

                    fileOutputStream.close();
                    inputStream.close();

                    Log.d("TCPClient", "File received");

                    //Decrypt the file
                    SecretKey aesKey = AESUtils.extractKeyFromBase64(aesKeyBase64);
                    IvParameterSpec iv = AESUtils.extractIVFromBase64(ivBase64);

                    AESUtils.decryptFile(receiveFile, outputFile, aesKey, iv);

                    //Delete the temp file
                    receiveFile.delete();
                    return 0;
                } catch (SocketTimeoutException e) {
                    return ERR_NETWORK_TIMEOUT;
                } catch (Exception e) {
                    Log.e("TCPClient", "Exception happens: ", e);
                    return ERR_OTHERS;
                }
            }
        });

        downloadStatThread.start();
        try {
            downloadStatThread.join();
        } catch (InterruptedException e) {
            return ERR_INTERRUPTED;
        }
        return result[0];
    }

    public int editModeRequest(String newMode, String newWaterAmount, String newScheduledFreq, String newScheduledTime, String serverPubkey) {
        final int[] result = new int[1];
        Thread editModeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = networkOperation();
            }

            public int networkOperation() {
                try (Socket socket = new Socket(serverHost, serverPort)) {
                    //Set the socket timeout
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    //InputStream is used for reading messages coming from server
                    InputStream in = socket.getInputStream();

                    //OutputStream is used for sending messages to the server
                    OutputStream out = socket.getOutputStream();

                    //Variables for sending and receiving
                    byte[] sendingBytes;
                    String sendingMessage;

                    byte[] receiveBuffer = new byte[1024];
                    int receiveLen;
                    String receivedMessage;
                    String recvCommand;
                    String[] recvParams;

                    //Send edit_mode(mode, water_amount, scheduled_freq, scheduled_time)
                    sendingMessage = "edit_mode(" + newMode + "," + newWaterAmount + "," + newScheduledFreq + ","+ newScheduledTime + ");";
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubkey);
                    out.write(sendingBytes);
                    out.flush();

                    //Receive finish_edit_server()
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    if(!receivedMessage.equals("finish_edit_server()")) {
                        return ERR_MESSAGE_CONTENT;   //Received message not correct
                    }

                    in.close();
                    out.close();

                } catch(SocketTimeoutException e) {
                    return ERR_NETWORK_TIMEOUT;
                } catch(Exception e) {
                    Log.e("TCPClient", "Exception happens: ", e);
                    return ERR_OTHERS;
                }
                return 0;
            }
        });
        editModeThread.start();
        try {
            editModeThread.join();
        } catch (InterruptedException e) {
            return ERR_INTERRUPTED;
        }

        return result[0];
    }

    public int wateringNowRequest(String serverPubkey) {
        final int[] result = new int[1];
        Thread wateringNowThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = wateringNetOperations();
            }

            private int wateringNetOperations() {
                try (Socket socket = new Socket(serverHost, serverPort)) {
                    //Set the socket timeout
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    //InputStream is used for reading messages coming from server
                    InputStream in = socket.getInputStream();

                    //OutputStream is used for sending messages to the server
                    OutputStream out = socket.getOutputStream();

                    //Variables for sending and receiving
                    byte[] sendingBytes;
                    String sendingMessage;

                    byte[] receiveBuffer = new byte[1024];
                    int receiveLen;
                    String receivedMessage;
                    String recvCommand;
                    String[] recvParams;

                    //Send watering_now() message
                    sendingMessage = "watering_now()";
                    Log.d("Server pubkey:", serverPubkey);
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubkey);
                    out.write(sendingBytes);
                    out.flush();

                    //Receive watering_succeed()
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    //Retrieve command
                    recvCommand = extractCommand(receivedMessage);
                    if(!recvCommand.equals("watering_succeed")) {
                        return ERR_MESSAGE_CONTENT;
                    }
                    in.close();
                    out.close();

                    return 0;
                } catch (SocketTimeoutException e) {
                    return ERR_NETWORK_TIMEOUT;
                } catch (Exception e) {
                    Log.e("TCPClient", "Exception happens:", e);
                    return ERR_OTHERS;
                }
            }
        });
        wateringNowThread.start();
        try {
            wateringNowThread.join();
        } catch (InterruptedException e) {
            return ERR_INTERRUPTED;
        }
        return result[0];

    }

    public int deleteDeviceRequest(String serverId, String serverPubkey) {
        final int[] result = new int[1];
        Thread delDeviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = networkOperation();
            }

            public int networkOperation() {
                try (Socket socket = new Socket(serverHost, serverPort)) {
                    //Set the socket timeout
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    //InputStream is used for reading messages coming from server
                    InputStream in = socket.getInputStream();

                    //OutputStream is used for sending messages to the server
                    OutputStream out = socket.getOutputStream();

                    //Variables for sending and receiving
                    byte[] sendingBytes;
                    String sendingMessage;

                    byte[] receiveBuffer = new byte[1024];
                    int receiveLen;
                    String receivedMessage;
                    String recvCommand;
                    String[] recvParams;

                    //Send del_device()
                    sendingMessage = "del_device()";
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubkey);
                    Log.d("Server pub key:", serverPubkey);
                    out.write(sendingBytes);
                    out.flush();

                    //Receive finish_del_device_server()
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    if(receivedMessage == null) {   //If encrypted or no end tag -> error
                        return 1;
                    }
                    //Retrieve command and arguments
                    recvCommand = extractCommand(receivedMessage);
                    if(!recvCommand.equals("finish_del_device_server")) {
                        return -1;
                    }

                    //Delete the device from client database
                    HICApplication.getInstance().deviceDatabaseHelper.deleteDevice(serverId);
                    //Close the input and output stream
                    in.close();
                    out.close();
                    return 0;

                } catch (SocketTimeoutException e) {
                    return ERR_NETWORK_TIMEOUT;
                } catch (Exception e) {
                    Log.e("TCPClient", "Exception happened:", e);
                    return ERR_OTHERS;
                }
            }
        });

        delDeviceThread.start();
        try {
            delDeviceThread.join();
        } catch (InterruptedException e) {
            return ERR_INTERRUPTED;
        }
        return result[0];
    }

    public int deleteStatRequest(String serverId, String serverPubkey) {
        final int[] result = new int[1];
        Thread deleteStatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = delStatNetOperations();
            }

            private int delStatNetOperations() {
                try (Socket socket = new Socket(serverHost, serverPort)) {
                    //Set the socket timeout
                    socket.setSoTimeout(SOCKET_TIMEOUT);

                    //InputStream is used for reading messages coming from server
                    InputStream in = socket.getInputStream();

                    //OutputStream is used for sending messages to the server
                    OutputStream out = socket.getOutputStream();

                    //Variables for sending and receiving
                    byte[] sendingBytes;
                    String sendingMessage;

                    byte[] receiveBuffer = new byte[1024];
                    int receiveLen;
                    String receivedMessage;
                    String recvCommand;
                    String[] recvParams;

                    //Send del_device()
                    sendingMessage = "del_stat()";
                    sendingBytes = packMessageEncrypt(sendingMessage, serverPubkey);
                    out.write(sendingBytes);
                    out.flush();

                    //Receive finish_del_stat_server()
                    receiveLen = in.read(receiveBuffer);
                    receivedMessage = unpackEncryptedMessage(receiveBuffer);
                    if(receivedMessage == null) {   //If encrypted or no end tag -> error
                        return ERR_MESSAGE_FORMAT;
                    }
                    //Retrieve command and arguments
                    recvCommand = extractCommand(receivedMessage);
                    if(!recvCommand.equals("finish_del_stat_server")) {
                        return ERR_MESSAGE_CONTENT;
                    }

                    //Close the input and output stream
                    in.close();
                    out.close();
                    return 0;

                } catch (SocketTimeoutException e) {
                    return ERR_NETWORK_TIMEOUT;
                } catch (Exception e) {
                    Log.e("TCPClient", "Exception happens:", e);
                    return ERR_OTHERS;
                }
            }
        });
        deleteStatThread.start();
        try {
            deleteStatThread.join();
        } catch (InterruptedException e) {
            return ERR_INTERRUPTED;
        }
        return result[0];
    }
    private byte[] packMessageNoEncrypt(String message) {
        //Create result array
        byte[] packedMessage = new byte[message.length() + 2];

        //Convert the input string to byte array
        byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);

        //Add the tag
        packedMessage[0] = 0x01;

        //Copy the input byte array to the result array
        System.arraycopy(messageBytes, 0, packedMessage, 1, messageBytes.length);   //Copy the message block to the packed array

        //Add the ending '\n'
        packedMessage[packedMessage.length - 1] = (byte)10;   //10 = '\n', set the last byte of the packed array

        //Log.println(Log.DEBUG, "packedMessage[end - 2]", String.valueOf(packedMessage[packedMessage.length - 2]));
        //Return the result
        return packedMessage;

    }



    private byte[] packMessageEncrypt(String message, String serverPubKey) {
        //Encrypt the message
        try {
            RSAUtils rsaUtils = RSAUtils.getInstance();
            String encryptedMessage = rsaUtils.rsaEncrypt(message, rsaUtils.loadPublicKey(serverPubKey));
            byte[] encryptedMessageBytes = packMessageNoEncrypt(encryptedMessage);
            encryptedMessageBytes[0] = 0x02;    //Change the tag
            return encryptedMessageBytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Unpack the received message from the receive buffer
    private String unpackUnencryptedMessage(byte[] receiveBuffer) {
        if(receiveBuffer[0] != (byte)0x01) {    //If encrypted -> error
            return null;
        }
        //byte endTag = receiveBuffer[receiveLen - 1];
        int indexOfEndTag = findIndexOf(receiveBuffer, (byte)10);
        if(indexOfEndTag == -1) {
            return null;    //The end of the message is not '\n', which is an error
        }
        //copy the message block to the array messageBlock
        byte[] messageBlock = new byte[indexOfEndTag];
        System.arraycopy(receiveBuffer, 1, messageBlock, 0, messageBlock.length);
        return new String(messageBlock);
    }

    private String unpackEncryptedMessage(byte[] receiveBuffer) {
        if(receiveBuffer[0] != 0x02) {  //If not encrypted -> error
            return null;
        }
        //Log.println(Log.ERROR, "Receive buffer Tag", "OK");
        int indexOfEndTag = findIndexOf(receiveBuffer, (byte)10);
        if(indexOfEndTag == -1) {
            return null;    //The end of the message is not '\n', which is an error
        }
        //Log.println(Log.ERROR, "Receive buffer n char", "OK");
        //copy the message block to the array messageBlock
        byte[] messageBlock = new byte[indexOfEndTag - 1];
        //Log.println(Log.ERROR, "messageblock length:", String.valueOf(indexOfEndTag));
        System.arraycopy(receiveBuffer, 1, messageBlock, 0, messageBlock.length);
        String messageBlockStr = new String(messageBlock);
        //String messageBlockStr = "fC8gXztvEAuxMrmvgeg8FQ5J5VRSLQj8OW/UV8S0XpSLR99a8EhxO4layFyAif5Hu/5IcCQVZHaDbNxCgB1DhfbDTt1RFeMP6LQ3Xv4XYOzsusC3rgkDrISgOJCQPf2N8fFGuSMORe/tVEQvQN0u0lyOToV1U/nDgpHPfvuwOR5LOK9TB/A+sFlEEA0gqi61zFkO1FfEMQii4laQcBPkLYQ5OajiDUrm9BDQIhruUj9Z/WE9JBjUFurameRCeBUjJQ6Pa+/da3ePAV2xazQCEmCH3JikX++h60qRyGeYnnz2x5yMnlNFEDsjj1e1CF5NuYu+3TyLRm1iuGeNqyvkmA==";

        String messagePT = "";
        try {
            RSAUtils rsaUtils = RSAUtils.getInstance();
            String privateKeyStr = rsaUtils.readKeyFromFile(false);
            //Log.println(Log.ERROR, "Private key:", privateKeyStr);
            PrivateKey rsaPrivateKey = rsaUtils.loadPrivateKey(privateKeyStr);
            Log.println(Log.ERROR, "Private key loaded", "OK");
            Log.println(Log.ERROR, "Cipher Text:", messageBlockStr);
            Log.println(Log.ERROR, "Cipher text length:", String.valueOf(messageBlockStr.length()));
            messagePT = rsaUtils.rsaDecrypt(messageBlockStr, rsaPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return messagePT;
    }

    private String extractCommand(String messagePT) throws IndexOutOfBoundsException {
        return messagePT.substring(0, messagePT.indexOf('('));
    }

    private String[] extractParams(String messagePT) throws IndexOutOfBoundsException {
        //If the substring (string between two braces) is empty, the split function will return an empty string array
        return messagePT.substring(messagePT.indexOf('(') + 1, messagePT.indexOf(')')).split(",");
    }

    public static int findIndexOf(byte[] array, byte target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i; // 返回找到的索引
            }
        }
        return -1; // 如果没有找到，返回-1
    }
}
