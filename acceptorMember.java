import java.net.ServerSocket;
import java.net.Socket;

public class acceptorMember{
    private ServerSocket socket;
    private Socket clientSocket;
    private int port;
    acceptorMember(int port){
        this.port = port;
    }
    public void startServer(){
        try {
           socket = new ServerSocket(port);
           System.out.println("starting on port "+port);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    void connect(){
        while(true){
            try {
                System.out.println("I am alive at port "+port);
                
                    clientSocket = socket.accept();
                    //span  out a new acceptor . 
                    acceptor accept = new acceptor(clientSocket);
                    Thread t = new Thread(accept);
                    t.start();
                
            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
    }

    public  static void main(String [] args){
        int port = Integer.parseInt(args[0]);
        acceptorMember p = new acceptorMember(port);
        p.startServer();
        p.connect();
    }
    
}
