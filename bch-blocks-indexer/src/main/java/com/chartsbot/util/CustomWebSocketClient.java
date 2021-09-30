package com.chartsbot.util;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.framing.Framedata;
import org.web3j.protocol.websocket.WebSocketClient;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class CustomWebSocketClient extends WebSocketClient {
    public interface  ReconnectHandlerInterface{
        void onReconnect();
    }
    ArrayList<ReconnectHandlerInterface> handlers=new ArrayList<>();
    @Synchronized
    private void doReconnect(){
        if(this.isClosed()){
            try {
                log.info("Had to reconnect websocket");
                Thread.sleep(1000);
                this.reconnectBlocking();
                for (ReconnectHandlerInterface handler : handlers) {
                    handler.onReconnect();
                }
            } catch (InterruptedException e) {
                log.info("Error in reconnect");
                Thread.currentThread().interrupt();
                throw new RuntimeException("Current thread needs to shutdown, Reconnect to websocket failed");
            }
        }
//        else {
//            String a = "5";
////            log.info("Not close");
//        }
    }
    public void addReconnectHandler(ReconnectHandlerInterface handler){
        handlers.add(handler);
    }
    public void removeReconnectHandler(ReconnectHandlerInterface handler){
        handlers.remove(handler);
    }
    public CustomWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public CustomWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void send(String text) throws NotYetConnectedException {
        doReconnect();
        super.send(text);
    }

    @Override
    public void send(byte[] data) throws NotYetConnectedException {
        doReconnect();
        super.send(data);
    }

    @Override
    public void sendPing() throws NotYetConnectedException {
        doReconnect();
        super.sendPing();
    }

    @Override
    @Synchronized
    public void onClose(int code, String reason, boolean remote) {
        if(remote || code!=1000){
            this.doReconnect();
            log.info("Reconnecting WebSocket connection to {}, because of disconnection reason: '{}'.",uri, reason);
        }else {
            super.onClose(code, reason, remote);
        }
    }

    @Override
    public void sendFragmentedFrame(Framedata.Opcode op, ByteBuffer buffer, boolean fin) {
        doReconnect();
        super.sendFragmentedFrame(op, buffer, fin);
    }

    @Override
    public void send(ByteBuffer bytes) throws IllegalArgumentException, NotYetConnectedException {
        doReconnect();
        super.send(bytes);
    }

    @Override
    public void sendFrame(Framedata framedata) {
        doReconnect();
        super.sendFrame(framedata);
    }

    @Override
    public void sendFrame(Collection<Framedata> frames) {
        doReconnect();
        super.sendFrame(frames);
    }
}