import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class acceptor implements Runnable{
    /*there are essentially 3 phases to commit a value, prepare , accept, commit. */
    /*acceptors accepts the message and sends a promise: “PROMISE <identifier>”  protocol
	Note:  the identifier is unique. 
    If the acceptor receives the prepare msg from the proposer with a lower 
    identifier than it has already promised before, it will ignore such requests and 
    send nothing or reject msg with the current identifier. The proposer can retry. 
    If the promise was made to another member, the promise msg will piggyback the value as well 
    in the message. As “PROMISE <identifier> <value>” protocol.

    Similarly an "ACCEPT <identifier> <value>" is sent for accept/commit phase.
    */
    
    private PrintWriter out;
    private BufferedReader in;
    public static ReentrantLock lock = new ReentrantLock();
    public static ReentrantLock lock2 = new ReentrantLock();
    public static Integer identifier = -1;
    public static String chosenOne = null;
    private  String acceptedValue = " ";
    acceptor(Socket clientSocket) throws IOException{
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new  PrintWriter(clientSocket.getOutputStream(), true);
    }
    //checks what the  message means and handles  them accordinglu
    //for prepare stage either a promise or a reject is sent
    //for accept/commit phase accept is sent or no message is  sent.
    void acceptMsg(String request){
        try {
            if(request.startsWith("PREPARE")){
                int decision = handlePrepareMSG(request);
                if(decision == 1){
                    //send a promise
                    System.out.println("promise was sent");
                }else if(decision == -1){
                    //send a reject
                    System.out.println("reject was sent");
                }
            }else if(request.startsWith("ACCEPT")){
               int decision = handleAcceptMSG(request);
               if(decision == 1){
                //send a accept
                System.out.println("accept was sent");
                }else if(decision == -1){
                    //send a reject
                    System.out.println("no message sent");
                }
            }else if(request.startsWith("COMMIT")){
                synchronized(lock2){
                    if(chosenOne == null){
                        chosenOne = request.split(" ")[1];
                    }
                    System.out.println("President is: "+ chosenOne);
                    out.println("President is: "+ chosenOne);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //handles the first phase: if the identifier received is less than currentIdentifier, send a reject else send a promise
    int handlePrepareMSG(String request){
        int answer = 1;
        String response = "PROMISE ";
        try {
            String [] rList = request.split(" ");
            Integer number= Integer.parseInt(rList[1]);
            synchronized(lock){
                System.out.println(number);
                if(number <= identifier){
                    answer = -1;
                    out.println("REJECT "+identifier.toString());
                    out.flush();
                    return answer;
                }else{
                    identifier = number;
                    out.println(response+number.toString()+" "+acceptedValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }
    //if  the identifier  receieved is less than the current, stall
    //else send the reply  with ACCEPT <identifier> <value>
    int handleAcceptMSG(String request){
        int answer = 1;
        String response = "ACCEPT ";
        try {
            String [] rList = request.split(" ");
            Integer number= Integer.parseInt(rList[1]);
            synchronized(lock){
                if(number < identifier){
                    answer = -1;
                   //stall this time.
                    return answer;
                }else{
                    identifier = number;
                    acceptedValue = rList[2];
                    synchronized(lock2){
                        if(chosenOne != null){
                            acceptedValue = chosenOne;
                        }
                    }
                    out.println(response+number.toString()+" "+acceptedValue);
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }
    
    @Override
    public void run(){
        try {
            //for all the requests made handle  them accordingly  and wait until the connection is alive
            //to  end the connection press Ctrl+C in the terminal. This will kill the acceptorMember at this port.
            String request;
            while((request = in.readLine())!=null){
                System.out.println(request);
                acceptMsg(request);
            }
        } catch (Exception e) {
          
            e.printStackTrace();
        }
    }
}
