package com.multiverse.ws;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

@Slf4j
public final class MultiverseWebSocketListener extends WebSocketListener {

    private ClientThread clientThread;
    private Client client;

    private boolean socketConnected;
    private boolean socketConnecting = true;

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private static int modIconsStart;

    public MultiverseWebSocketListener(int iconsStartIndex, Client gameClient, ClientThread gameClientThread) {
        modIconsStart = iconsStartIndex;
        client = gameClient;
        clientThread = gameClientThread;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.debug("Connected");
        socketConnecting = false;
        socketConnected = true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String jsonString) {
        JsonObject data = new JsonParser().parse(jsonString).getAsJsonObject();
        handleWebsocketMessage(data);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("Receiving: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        System.out.println("Closing: " + code + " " + reason);
        socketConnecting = false;
        socketConnected = false;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
        socketConnecting = false;
        socketConnected = false;
    }

    public boolean isSocketConnected() {
        return socketConnected;
    }

    public boolean isSocketConnecting() {
        return socketConnecting;
    }

    public void handleWebsocketMessage(JsonObject data) {
        String platform = data.get("platform").getAsString();
        String name = data.get("name").getAsString();
        String message = data.get("message").getAsString();

        if (!message.equals("")) {
            clientThread.invokeLater(() -> {
                if (platform.equals("discord")) {
                    client.addChatMessage(ChatMessageType.FRIENDSCHAT, Text.toJagexName(name), String.format("<col=5a6db0>%s</col>", message), String.format("<img=%d>", modIconsStart));
                } else if (platform.equals("plugin")) {
                    client.addChatMessage(ChatMessageType.FRIENDSCHAT, Text.toJagexName(name), String.format("<col=17686e>%s</col>", message), "<img=227>");
                }
            });
        }
    }
}