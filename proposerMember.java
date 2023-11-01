import java.io.IOException;


public class proposerMember {
    //member sends out the messages to the other members/starts the conversation
    private String myName;
 
    public static Float identifier = (float) 1.0;
    proposerMember(String name){
        myName = name;
       
        
    }
    void run() throws IOException{
        for(int i  = 0; i < 6;  i++){
            proposer p = new proposer(myName,"localhost", 1025+i);
            
            Thread t = new Thread(p);
            t.start();
            
        }
        
    }
    public static void main(String [] args) throws IOException{
        proposerMember m = new proposerMember(args[0]);
        m.run();
    }
    
}
