import java.net .*;
import java.io.*;
import java.util.*;

public class Host
{
  static List<String> addresses = new ArrayList<>();
  static Scanner input = new Scanner(System.in);
  static volatile boolean running = false;
  
  private static String selectAddress(String prompt)
  {
    int count = 1;
    for(String IP : addresses)
    {
      System.out.println(count + ".\t" + IP);
      ++count;
    }
    System.out.print(prompt);
    int position = Integer.parseInt(input.nextLine());
    if(position >= addresses.size() || pos < 1)
      return null;
    return addresses.get(position - 1);
  }
  
  private static void loadAddress()
  {
    Runtime runtime = Runtime.getRuntime();
    Process proc = runtime.exec("ip neigh show");
    proc.waitFor();
    
    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String line;
    
    while((line = reader.readLine()) != null)
    {
      String[] splits = line.split(" ");
      if(splits.length < 4)
        continue;
      
      if(splits[0].contains("192.168.43."))
        addresses.add(splits[0]);
    }
  }
  
  public static void main(string ... args) throws Exception
  {
    System.out.println("CHECKERS HOST");
    loadAddress();
    int PORT_ONE = 2807, PORT_TWO = 7082;
    
    try
    {
      String line = selectAddress();
      if(line == null)
        throw new IllegalStateException("Null IP Address");
      
      Socket s1 = new Socket(line, PORT_ONE);
      BufferedReader r1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
      PrintWriter w1 = new PrintWriter(s1.getOutputStream());
      
      System.out.println("Connected to Player One");
      
      String line2 = selectAddress();
      if(line2 == null)
        throw new IllegalStateException("Null IP Address");
      else if(line2.equals(line))
        throw new IllegalStateException("Same IP Address Picked");
      
      Socket s2 = new Socket(line2, PORT_TWO);
      BufferedReader r2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
      PrintWriter w2 = new PrintWriter(s2.getOutputStream());
      
      System.out.println("Connected to Player Two");
      System.out.println("\nBinding Player One and Player Two");
      
      running = true;
      
      new Thread(() ->
                 {
                   while(running) 
                   {
                     try 
                     {
                       String msg = r1.readLine();
                       w2.println(msg);
                       w2.flush();
                     }
                     catch(Exception ex)
                     {
                       running = false;
                       System.out.println("Error: " + ex.getMessage());
                       break;
                     }
                   }).start();
                   
      while(running)
      {
        try 
        {
          String msg = r2.readLine();
          w1.println(msg);
          w1.flush();
        }
        catch(Exception ex)
        {
          running = false;
          System.out.println("Error: " + ex.getMessage());
          break;
         }
      }
      
      catch(Exception ex)
      {
        System.out.println("Error: " + ex.getMessage();
      }
  }
}
