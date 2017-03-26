package httpserver;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.regex.*;

/**
 * Created by lisa on 25.3.17.
 */

public class HTTPserver {
    private JFrame frm;
    private JPanel pan;
    private JTextArea textArea;

    public static void main(String[] args) throws Throwable {
        HTTPserver httpserver = new HTTPserver();
        httpserver.go();
    }
    public void go(){
        setGUI();
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client accepted");
                new Thread(new SocketProcessor(socket)).start();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setGUI(){
        frm = new JFrame();
        pan = new JPanel();
        textArea = new JTextArea(20, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        JScrollPane scr = new JScrollPane(textArea);
        scr.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pan.add(scr);

        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setContentPane(pan);
        frm.setBounds(10, 10, 10, 10);
        frm.setSize(600, 400);
        frm.setVisible(true);
    }

    private class SocketProcessor implements Runnable {

        private Socket socket;
        private InputStream is;
        private OutputStream os;

        private SocketProcessor(Socket s) {
            try {
                this.socket = s;
                this.is = socket.getInputStream();
                this.os = socket.getOutputStream();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void run() {
            try {
                readInputHeaders();
                writeResponse("<html><body><h1>Hello you</h1></body></html>");
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                    socket.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.out.println("Client processing finished");
        }

        private void writeResponse(String s) throws Throwable {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: LisaServer\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            String result = response + s;
            os.write(result.getBytes());
            os.flush();
            textArea.append("\n\n SERVER RESPONSE \n");
            textArea.append(result);
        }

        private void readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while(true) {
                String s = br.readLine();
                if(s == null || s.trim().length() == 0) {
                    break;
                }
                parseLine(s);
                textArea.append("\n");
                textArea.append(s);
            }
        }

        public void parseLine(String s){
            Pattern pattern = Pattern.compile("GET (.*) HTTP/1.1");
            Matcher matcher = pattern.matcher(s);
            if (matcher.matches()){
                System.out.println("Matches: " + matcher.group());
                System.out.println(matcher.groupCount() + " " +matcher.group(1));
                tryToFind("./src/files", "first.html");
            }
        }

        public void tryToFind(String dir, String fname){
            File file = new File(dir, fname);

            try(    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis)){
                byte [] mybytearray  = new byte [(int)file.length()];
                bis.read(mybytearray,0,mybytearray.length);
                os.write(mybytearray,0,mybytearray.length);
                os.flush();
                textArea.append(mybytearray.toString());
                System.out.println("Done.");
            }catch (Exception ex){
                System.out.println("Sorry( couldn't find your file");
                //ex.printStackTrace();
            }
        }
    }
}
