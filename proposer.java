import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;


public class proposer implements Runnable{
    // In the 2 phase commit, the proposer have two main components 
    //1. Prepare stage.
        // in this stage the proposer has a number and  sends it over the network. 
        //and waits for a response. If the response  is a reject and a number, the proposer adds 1 and sends the proposal again.
        // if the response has a Promise, a number and a value, it goes to the next stage of accept phase,  after committing its local president value to the value receieved.
        // if however it gets accepted it sends the value(chosenPresident/name) as is, without any updates,  as it gets the "Promise and the value" msg
    //2. Accept stage.
        //proposer sends the update request with "ACCEPT REQUEST number Value"
        //this value is committed by the acceptor and an acknowledgement might  be sent.
    //3. Commit stage. 
        //when the quorom is reached, the proposer sends a commit to all the nodes. 
    public static Integer identifier = 1;
    public static String chosenPresident = null;
    private String chosenValue ;
    public static ReentrantLock lock = new ReentrantLock();
    public static ReentrantLock lock2 = new ReentrantLock();
    public static ReentrantLock lock3 = new ReentrantLock();
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    public static int readyToSend = 0;
    String myName;
    String hostname;
    int port;
    private int tries = 3;
    color Color = new color();
    public static String F;
    
    
    proposer(String name, String hostname, int port){
        this.myName = name;
        this.hostname = hostname;
        this.port = port;
        this.chosenValue  = myName;
        
    }
    // public void setIdentifier(Float identifier) throws IOException {
    //     synchronized(lock){proposer.identifier = Math.max(proposer.identifier,identifier);
    //         identifier  = proposer.identifier;
    //     }
    //     synchronized(lock3){
    //         F = "backlog"+myName;
    //         Long i = System.currentTimeMillis();
    //         File f = new File(F, i.toString());
    //         i++;
    //         FileWriter fileWriter  = new FileWriter(f);
    //         fileWriter.write(myName + " identifier is getting set: "+identifier.toString()+"\n");
    //         fileWriter.close();
    //     }
       
    // }
    //connects with the desired host,port
    void connectWith(String hostname, int port){
        try {
            socket = new Socket(hostname, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);

        } catch(ConnectException e){
            System.err.println(Color.red+"the host is not alive or sleeping at port "+ port+Color.reset);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // prepare message is to ask  for a promise in return. Try until a promise is received
    void PrepareMsg(){
        try {
            synchronized(lock){
                out.println("PREPARE "+identifier.toString());
                out.flush();
            }
            // synchronized(lock3){
            //     Long i = System.currentTimeMillis();
            //     File f = new File(F, i.toString());
            //     i++;
            //     FileWriter fileWriter = new FileWriter(f);
                
            //     fileWriter.append(myName+": sending prepare msg--> PREPARE "+identifier+"\n");
            //     fileWriter.close();
            // }
            // out.flush();
            //time this msg
            int timeout = 1000;
            socket.setSoTimeout(timeout*2);
            //if time out occurs it is caught as an Exception and handled seperately below.
            String response = in.readLine();
            while(response.startsWith("REJECT")){
                //just for variability and randomness.
                Random random = new Random();
                int randomValue = random.nextInt(timeout)+1000;
                Thread.sleep(randomValue);
                synchronized(lock){
                       
                    identifier = Integer.parseInt(response.split(" ")[1])+1;
                    
                    out.println("PREPARE "+identifier.toString());
                    out.flush();
                    // synchronized(lock3){
                    //     Long i = System.currentTimeMillis();
                    //     File f = new File(F, i.toString());
                    //     i++;
                    //     FileWriter  fileWriter = new FileWriter(f);
                    //     fileWriter.append("response received"+response+"\n"); 
                    //     fileWriter.append("and now the new identifier i try with is: "+identifier.toString());
                    //     fileWriter.close();
                    // }
                    
                }
                
                response = in.readLine();
                
            }
            
            if(response.startsWith("PROMISE")){
                String [] responsesString = response.split(" ");
                
                if(responsesString.length == 2){
                    synchronized(lock){
                        
                        readyToSend++;
                        identifier = Math.max(Integer.parseInt(responsesString[1]),identifier);
                        
                     
                    }
                    // synchronized(lock3){
                    //     Long i = System.currentTimeMillis();
                    //     File f = new File(F, i.toString());
                    //     i++;
                    //     FileWriter fileWriter  = new FileWriter(f);
                    //     fileWriter.append(i.toString()+response);
                    //     fileWriter.close();
                    // }
                }else if(responsesString.length == 3){
                    synchronized(lock){
                        
                        readyToSend++;
                        identifier = Math.max(Integer.parseInt(responsesString[1]),identifier);
                        chosenValue = responsesString[2];
                        

                    }
                    // synchronized(lock3){
                    //     Long i = System.currentTimeMillis();
                    //     File f = new File(F, i.toString());
                    //     i++;
                    //     FileWriter fileWriter  = new FileWriter(f);
                    //     fileWriter.append(response);
                    //     fileWriter.close();
                    // }
                }else{
                    throw new IOException(Color.red+"THE RESPONSE RECEIVED GOT DAMAGED!"+Color.reset);
                }
            }else{
                throw new IOException(Color.red+" I was not expecting this message at all!!"+Color.reset);
            }
        } catch(SocketTimeoutException e){
            //if timed out retry;
            System.err.println(Color.red+"Timed out without any data received. Maybe retry again??"+Color.reset);
            tries--;
            synchronized(lock){identifier++;}
            if(tries > 0){
                PrepareMsg();
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    String acceptPhaseMsg(){
        String  acceptedValue = null;
        try {
            synchronized(lock){
                out.println("ACCEPT-REQUEST "+identifier.toString()+" "+chosenValue);
                out.flush();
            }
            int timeout = 2000;
            socket.setSoTimeout(timeout);

            //expecting "ACCEPT <identifier> <value>" or no response.
            //if no response is receieved for a while return null
            String response = in.readLine();
            String [] responsesString = response.split(" ");

            // synchronized(lock3){
            //     Long i = System.currentTimeMillis();
            //     File f = new File(F, i.toString());
            //             i++;
            //             FileWriter fileWriter  = new FileWriter(f);
            //     fileWriter.append(response);
            //     fileWriter.close();
            // }
            if(responsesString.length == 3){//means we got the correct msg as "ACCEPT <identifier> <value>"

                synchronized(lock){
                    //Long i = System.currentTimeMillis();
                    identifier = Math.max(Integer.parseInt(responsesString[1]),identifier);
                    acceptedValue = responsesString[2];
                    // File file = new File(myName+i+".txt");
                    // FileWriter writer = new FileWriter(file);
                    // writer.write(chosenPresident+" is the elected president\n");
                    
                    // writer.close();
                }
                // synchronized(lock3){
                //     Long i = System.currentTimeMillis();
                //     File f = new File(F, i.toString());
                //     i++;
                //     FileWriter fileWriter  = new FileWriter(f);
                //     fileWriter.append(chosenPresident+" is the elected president\n");
                //     fileWriter.close();
                // }
            }else{
                throw new IOException(Color.red+"THE RESPONSE RECEIVED GOT DAMAGED at accept phase!"+Color.reset);
            }
        } catch(SocketTimeoutException e){
            System.err.println(Color.red+" The session timed out during the second phase."+Color.reset);
            System.err.println("trying again .. .. ..");
            return acceptedValue;//null
        }catch (Exception e) {
            e.printStackTrace();
        }

        return acceptedValue;
    }

    public static Map<String, Integer> myMap  = new HashMap<String, Integer>();
    public static Integer count = 0;
    public static Boolean consensusReached =   false;
    public static Integer runningCount = 0;
    public static ReentrantLock lock4 = new ReentrantLock();
    @Override
    public void run() {
        try {
            synchronized(lock4){
                runningCount++;
                if(runningCount%7 == 1){
                    myMap = new HashMap<>();
                    count = 0;
                    consensusReached = false;
                }
            }
            if(socket == null){connectWith(hostname, port);}
            if(socket != null){
                PrepareMsg();
            }else{
                throw new IOException(Color.red+"Retry after some time to send the email again to the port: "+port+Color.reset);
                
            }
            
            synchronized(lock2){
                //System.out.println("readyTosend by name: "+myName+"is: "+readyToSend);
                while(readyToSend < 4){
                    lock2.wait();
                }
                lock2.notifyAll();
                //System.out.println(readyToSend+"after notification!");
            }
            
            //as soon as someone comes with an accepted value, check if there exists a majiority.
            //if not wait for other threads to return. When the other threads return, step 1 is followed, until all  the 7  accept request finished. 
            // if no quorom is established, run with the proposals again.
            //else if the quorm is reached, a consensus is reached, a chosenValue  is  sent to the  learners  and the acceptors. 
            //start the commit phase.
            //wait until i receive the reply from all of the threads

            String key = acceptPhaseMsg();
            synchronized(lock3){
                count++;
                if(key != null && myMap.containsKey(key)){
                    myMap.put(key, ((myMap.get(key))+1));
                }else if(key != null){
                    myMap.put(key, 1);
                }
                if( key!=  null && myMap.get(key)>3){
                    consensusReached = true;
                    chosenPresident = key;
                    lock3.notifyAll();
                }
                while(consensusReached ==false && count%7 < 7){
                    lock3.wait();
                }
                if(consensusReached == false  && count%7 == 0){
                    notifyAll();
                    run();
                }
                if(consensusReached == true){
                     //start the commit phase
                     System.out.println("starting the commit phase for chosenPresident: "+chosenPresident);
                     commitphase(chosenPresident);
                }
            }
            


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void commitphase(String value){
        try {
            out.println("COMMIT "+value);
            out.flush();
            System.out.println(Color.green+"Committing "+value+Color.reset);
            String printThis = in.readLine();
            System.out.println(Color.green+printThis+Color.reset);
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println("COMMIT  pHASE failed");
            e.printStackTrace();
        }
    }
}