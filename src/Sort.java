import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

public class Sort {
	
	static File origem = new File("C:\\Users\\lucas.alves\\Downloads\\huge.log");
	static File destino = new File("C:\\Users\\lucas.alves\\Downloads\\sorted.log");
	
	//Sera utilizado o external sort pois o tamanho do dado não cabe na memoria 
	//primeiramente precisamos definir o tamanho dos blocos para fazer a divisão do arquivo
	//em arquivos temporarios
	public static long estimateBestSizeOfBlocks(File filetobesorted) {
	        long sizeoffile = filetobesorted.length();
	        final int MAXTEMPFILES = 1024;
	        long blocksize = sizeoffile / MAXTEMPFILES ;
	        long freememory = Runtime.getRuntime().freeMemory();
	        //se o tamanho dos blocos for menor que metade da memoria aumenta ele 
	        if( blocksize < freememory/2)
	            blocksize = freememory/2;
	        else {
	            if(blocksize >= freememory) 
	              System.err.println("a memória talvez acabe ");
	        }
	        return blocksize;
	    }
	 
	//com o tamanho dos blocos estimados esse metodo ira carregar o arquivos por blocos
	//de n linhas, ordenalos , e escrever o resultado em arquivos temporarios.
	public static List<File> sortInBatch(File file, Comparator<String> cmp) throws IOException {
        List<File> files = new ArrayList<File>();
        BufferedReader fbr = new BufferedReader(new FileReader(file));
        long blocksize = estimateBestSizeOfBlocks(file);
        try{
            List<String> tmplist =  new ArrayList<String>();
            String line = "";
            try {
                while(line != null) {
                    long currentblocksize = 0;
                    //esta no loop ate que o tamanho do blocotual não ultrapasse o definido
                    while((currentblocksize < blocksize) 
                    &&(   (line = fbr.readLine()) != null) ){ 
                        tmplist.add(line);
                        currentblocksize += line.length(); 
                    }
                    //chama o metodo sort and save que faz a ordenação 
                    //e salva em um arquivo temporario
                    files.add(sortAndSave(tmplist,cmp));
                    tmplist.clear();
                }
            } catch(EOFException oef) {
                if(tmplist.size()>0) {
                    files.add(sortAndSave(tmplist,cmp));
                    tmplist.clear();
                }
            }
        } finally {
            fbr.close();
        }
        return files;
    }
	public static File sortAndSave(List<String> tmplist, Comparator<String> cmp) throws IOException  {
        Collections.sort(tmplist,cmp);  // 
        File newtmpfile = File.createTempFile("sortInBatch", "flatfile");
        newtmpfile.deleteOnExit();
        BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile));
        try {
            for(String r : tmplist) {
                fbw.write(r);
                fbw.newLine();
            }
        } finally {
            fbw.close();
        }
        return newtmpfile;
    }
	//esse metodo mescla varios arquivos temporarios 
	//utilizando um comparador que mostra como as listas devem ser ordenadas 
	
	 public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp) throws IOException {
	        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, 
	            new Comparator<BinaryFileBuffer>() {
	              public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
	                return cmp.compare(i.peek(), j.peek());
	              }
	            }
	        );
	        for (File f : files) {
	            BinaryFileBuffer bfb = new BinaryFileBuffer(f);
	            pq.add(bfb);
	        }
	        BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
	        int rowcounter = 0;
	        try {
	            while(pq.size()>0) {
	                BinaryFileBuffer bfb = pq.poll();
	                String r = bfb.pop();
	                fbw.write(r);
	                fbw.newLine();
	                ++rowcounter;
	                if(bfb.empty()) {
	                    bfb.fbr.close();
	                    bfb.originalfile.delete();
	                } else {
	                    pq.add(bfb); 
	                }
	            }
	        } finally { 
	            fbw.close();
	            for(BinaryFileBuffer bfb : pq ) bfb.close();
	        }
	        return rowcounter;
	    }
	 
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		//parametro
		 Comparator<String> comparator = new Comparator<String>() {
	            public int compare(String r1, String r2){
	                return r1.compareTo(r2);}};
	                List<File> list= sortInBatch(origem,comparator);
	                mergeSortedFiles(list,destino,comparator); 
	                
	}

}
class BinaryFileBuffer  {
    public static int BUFFERSIZE = 2048;
    public BufferedReader fbr;
    public File originalfile;
    private String cache;
    private boolean empty;
     
    public BinaryFileBuffer(File f) throws IOException {
        originalfile = f;
        fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
        reload();
    }
     
    public boolean empty() {
        return empty;
    }
     
    private void reload() throws IOException {
        try {
          if((this.cache = fbr.readLine()) == null){
            empty = true;
            cache = null;
          }
          else{
            empty = false;
          }
      } catch(EOFException oef) {
        empty = true;
        cache = null;
      }
    }
     
    public void close() throws IOException {
        fbr.close();
    }
     
     
    public String peek() {
        if(empty()) return null;
        return cache.toString();
    }
    public String pop() throws IOException {
      String answer = peek();
        reload();
      return answer;
    }
     
     
 
}
