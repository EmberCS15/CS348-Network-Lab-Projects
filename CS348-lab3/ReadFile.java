import java.io.BufferedReader;
import java.io.FileReader;

public class ReadFile {
    public String read(String fileName) throws Exception{
        FileReader fileReader = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fileReader);
        String r = br.readLine()+"\n";
        String l="";
        while(r!=null){
            l+=r+"\n";
            r=br.readLine();
        }
        br.close();
        fileReader.close();
        return l;
    }
}
