package zeromq.distributed.cache;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;

public class ZeroMQProxy {

    private static List<CacheSegment> serverList = new ArrayList<>();

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket frontend = context.socket(SocketType.ROUTER);
        ZMQ.Socket backend = context.socket(SocketType.ROUTER);
        frontend.bind("tcp://*:5559");
        backend.bind("tcp://*:5560");
        System.out.println("launch and connect broker.");
// Initialize poll set
        ZMQ.Poller items = context.poller(2);
        items.register(frontend, ZMQ.Poller.POLLIN);
        items.register(backend, ZMQ.Poller.POLLIN);
        boolean more = false;
        byte[] message;
// Switch messages between sockets
        while (!Thread.currentThread().isInterrupted()) {
// poll and memorize multipart detection
            items.poll();
            if (items.pollin(0)) {
                while (true) {
                    message = frontend.recv(0);
                    more = frontend.hasReceiveMore();
                    backend.send(message, more ? ZMQ.SNDMORE : 0);
                    if (!more) {
                        break;
                    }
                }
            }
            if (items.pollin(1)) {
                    message = backend.recv(0);
                    String strMsg = new String(message);
                    String [] msg = parseString(strMsg);

                    if(msg[0].equals("NOTIFY")) {
                        CacheSegment insert = new CacheSegment(Integer.parseInt(msg[1]), Integer.parseInt(msg[2]));
                        if(!serverList.contains(insert))
                            serverList.add(new CacheSegment(Integer.parseInt(msg[1]), Integer.parseInt(msg[2])));
                    }
                    more = backend.hasReceiveMore();
                    frontend.send(message, more ? ZMQ.SNDMORE : 0);
                    if (!more) {
                        break;
                    }
            }

        }

    }
    private static String[] parseString(String str){
        return str.split(" ");
    }
    private static void checkList(){

    }
}
