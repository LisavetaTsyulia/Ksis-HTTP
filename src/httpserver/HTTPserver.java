package httpserver;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.Calendar;
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

        private String writeHeader(String s, int code, String description){
            String response = "HTTP/1.1 "+ code + " " + description + "\r\n" +
                    "Date: "+ Calendar.getInstance().getTime() +"\r\n" +
                    "Server: LisaServer\r\n" +
                    "Last-Modified: \r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            return response;
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
            Pattern pattern = Pattern.compile("GET\\s(.*)\\sHTTP/1.1");
            Pattern patternHead = Pattern.compile("HEAD\\s(.*)\\sHTTP/1.1");
            Pattern patternPost = Pattern.compile("POST\\s(.*)\\sHTTP/1.1");
            Matcher matcher = pattern.matcher(s);
            if (matcher.matches() && matcher.group(1) != "/favicon.ico"){
                System.out.println("Matches: " + matcher.group());
                System.out.println(matcher.groupCount() + " " + matcher.group(1));
                getFunction("./src/files", matcher.group(1));
            }else if((matcher = patternHead.matcher(s)).matches()) {
                System.out.println("Matches: " + matcher.group());
                System.out.println(matcher.groupCount() + " " + matcher.group(1));
                headFunction("./src/files", matcher.group(1));
            }else if((matcher = patternPost.matcher(s)).matches()){
                System.out.println("Matches: " + matcher.group());
                System.out.println(matcher.groupCount() + " " + matcher.group(1));
                postFunction();
            }
        }

        public void getFunction(String dir, String fname){
            File file = new File(dir, fname);
            if(file.exists() && !file.isDirectory()) {
               sendFile(file, 200, "OK");
            } else {
                File file2 = new File("./src/files", "/sorry.html");
                sendFile(file2, 404, "FILE NOT FOUND");
            }

        }
        public void sendFile(File file, int code, String description){
            try(    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis)){
                byte [] mybytearray  = new byte [(int)file.length()];
                bis.read(mybytearray,0,mybytearray.length);
                String s = new String(mybytearray);
                String result = (writeHeader(s, code, description) + s);
                os.write(result.getBytes());
                os.flush();
                textArea.append("RESPONSE:\n" + result);
                System.out.println("Done.");
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void headFunction(String dir, String fname){

        }

        public void postFunction(){

        }
    }
}
