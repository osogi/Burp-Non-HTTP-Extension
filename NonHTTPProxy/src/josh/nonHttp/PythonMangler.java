package josh.nonHttp;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


import org.python.core.PyBoolean;
import org.python.core.PyByteArray;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class PythonMangler {
	private HashMap<Integer,GenericMiTMServer> threads = new HashMap<Integer,GenericMiTMServer>();
	private String pyCode;
	private PythonInterpreter interpreter;
	private List _listeners = new ArrayList();
	private String out = "";
	private String err ="";
	private String scripts;
	private boolean python3using;
	private String pythonpath;

	public String getError(){
		String tmp  = err;
		err = "";
		return tmp;
	}
	public String getOutput(){
		String tmp  = out;
		out ="";
		return tmp;
	}

	public HashMap<String,Object> runRepeaterCode(String code){
		String errors="";
		String outer="";
		String buf="";
		String term= System.getenv("TERM");
		if (term==null) term="linux";
		HashMap<String,Object> outputs = new HashMap<String,Object>();
		byte[][] output = new byte[0][0];
		try{
			/*interpreter.exec(code);
			PyObject someFunc = interpreter.get("sendPayload");
			if(someFunc == null)
				return null;
			PyObject result = someFunc.__call__();
			PyByteArray array = (PyByteArray) result.__tojava__(Object.class);

			output = new byte [array.__len__()];
			for(int i=0; i < array.__len__(); i++){
				output[i] = (byte)array.get(i).__tojava__(Byte.class);
			}*/
			PrintWriter writer = new PrintWriter(scripts+ "replier.py", "UTF-8");
			writer.println("import NoPy");
			writer.println("");
			writer.println(code);
			writer.println("");
			writer.println("def reply(a, C2S):\n" +
					"\tglobal super_secret_var_3120_ugrabipki\n" +
					"\tsuper_secret_var_3120_ugrabipki+=NoPy.reply(a, C2S)  " +
					"\n" +
					"super_secret_var_3120_ugrabipki=\"\"\n" +
					"print(NoPy.toHex(sendPayload())+super_secret_var_3120_ugrabipki)");
			writer.close();
			Process proc;
			if(!isWindows()){
			writer = new PrintWriter(scripts+ "test", "UTF-8");
			writer.println("export TERM="+term);
			writer.println(pythonpath + " \""+scripts+"replier.py\"");
			writer.close();
			proc = Runtime.getRuntime().exec("sh "+scripts+ "test");
			}
			else {
			proc = Runtime.getRuntime().exec(pythonpath + " \""+scripts+"replier.py\"");
			}


			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				outer+=buf+"\n";
				buf=line;

			}
			reader.close();

			reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			line = null;
			while ((line = reader.readLine()) != null)
			{
				errors+=line+"\n";
			}
			reader.close();

			proc.destroy();
			//output=fromHexString(buf);
			output=outputRecognise(buf);
			outer=outer.substring(1);
		}catch(Exception ex){
			ex.printStackTrace();
			output=null;
		}

		outputs.put("out", output);
		outputs.put("stdout", outer);
		outputs.put("stderr", errors);
		return outputs;

	}


	public PythonMangler(){

		pythonpath = this.getProperties("python3using", ""); //python3path as a buf
		if((!pythonpath.equals("false"))||(pythonpath == null )) python3using=true;
		else python3using=false;
		if(python3using){
			pythonpath=this.getProperties("python3path", "");
		}
		else{
			pythonpath=this.getProperties("python2path", "");
		}
		String path = System.getProperty("user.home");
		scripts = path + "/.NoPEProxy/py_scripts/";
		File d = new File(scripts);
		if(!d.exists()) {

			d.mkdir();
			try {
				PrintWriter writer = new PrintWriter(scripts + "NoPy.py", "UTF-8");
				writer.println("def reply(bytes, C2S):\n" +
						"\tbuf='c'\n" +
						"\tif(C2S):\n" +
						"\t\tbuf='s'\n" +
						"\treturn '#'+\"{:02x}\".format(ord(buf))+toHex(bytes)" +
						"\n" +
						"def toHex(bytes):\n" +
						"\treturn \"\".join(\"{:02x}\".format(c) for c in bytes)\n" +
						"\n" +
						"def fromHex(string):\n" +
						"\treturn bytearray.fromhex(string)");
				writer.close();
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		//String fs =  System.getProperty("file.separator");
		//String file = System.getProperty("user.dir") + fs  +"mangler.py";
		//String path = System.getProperty("user.home");
		String file = path + "/.NoPEProxy/mangler.py";
			/*Properties props = new Properties();
			System.out.println(System.getProperty("python.path"));
			props.setProperty("python.path", System.getProperty("user.dir"));
			PythonInterpreter.initialize(System.getProperties(), props,
                    new String[] {""});*/

		//this.interpreter = new PythonInterpreter();
		//TODO: Add output steam to this so that we can log errors to the console.
		/*interpreter.setOut(out);
		interpreter.setErr(err);*/

		File f = new File(file);
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		Path p = Paths.get(file);

		BufferedReader reader;
			/*pyCode="import sys\r\nsys.path.append('" + System.getProperty("user.dir") + "')\r\n"
					+ "libs=['C:\\Python27\\Lib\\site-packages', 'C:\\Python27\\Lib\\site-packages\\pypcap-1.1.5-py2.7-win32.egg', 'C:\\WINDOWS\\SYSTEM32\\python27.zip', 'C:\\Python27\\DLLs', 'C:\\Python27\\Lib', 'C:\\Python27\\Lib\\plat-win', 'C:\\Python27\\Lib\\lib-tk', 'C:\\Python27']\r\n"
					+ "for lib in libs:\r\n"
					+ "   sys.path.append(lib)\r\n\r\n"
					+ "print sys.path\r\n";*/
		pyCode="";

		try {
			reader = Files.newBufferedReader(p);

			String line="";
			while ((line = reader.readLine()) != null) {
				pyCode+=line+"\r\n";
			}
			if(pyCode.trim().equals("")){
				pyCode= "def mangle(bytes, isC2S):\r\n";
				pyCode+="\treturn bytes";
				p = Paths.get(file);
				Charset charset = Charset.forName("UTF-8");
				try (BufferedWriter writer = Files.newBufferedWriter(p, charset)) {
					writer.write(pyCode);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		} catch (IOException e) {
			pyCode="";
			e.printStackTrace();
		}


	}

	public String getPyCode(){
		return pyCode.replaceAll("\r", "");
	}
	public String setPyCode(String code){
		pyCode=code;
		////String fs =  System.getProperty("file.separator");
		//String file = System.getProperty("user.dir") + fs + "mangler.py";
		String path = System.getProperty("user.home");
		String file = path + "/.NoPEProxy/mangler.py";
		this.pyCode = code;
		if(pyCode.trim().equals("")){
			pyCode= "def mangle(input, isC2S):\n";
			pyCode+="\treturn input\n";
		}
		Charset charset = Charset.forName("UTF-8");
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.println(code);
			writer.close();
			writer = new PrintWriter(scripts+ "main.py", "UTF-8");
			writer.println( "import NoPy\n" +
							"import sys\n");
			writer.println("");
			writer.println(code);
			writer.println("");
			writer.println("\n" +
					"\n" +
					"def reply(a):\n" +
					"\tglobal super_secret_var_3120_ugrabipki\n" +
					"\tglobal isC2S\n" +
					"\tsuper_secret_var_3120_ugrabipki+=NoPy.reply(a, isC2S) " +
					"\n" +
					"super_secret_var_3120_ugrabipki=\"\"\n" +
					"inp=sys.argv[1]\n" +
					"reg=inp[:3]\n" +
					"isC2S=False\n" +
					"if(inp[3:4]=='1'):\n" +
					"\tisC2S=True\n" +
					"orig=NoPy.fromHex(inp[4:])\n" +
					"result=\"\"\n" +
					"if(reg==\"mng\"):\n" +
					"\tresult=NoPy.toHex(mangle(orig, isC2S))\n" +
					"elif(reg==\"pst\"):\n" +
					"\tresult=NoPy.toHex(postIntercept(orig, isC2S))\n" +
					"else:");
			if (python3using)
			writer.println("\tprint(\"NoPEproxy error: main.py: Unknown mode - \\'\"+reg+\"\\'\", file=sys.stderr)");
			else writer.println("\tprint >> sys.stderr, \"NoPEproxy error: main.py: Unknown mode - \\'\"+reg+\"\\'\"");
			writer.println("print(result+super_secret_var_3120_ugrabipki)");
			writer.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	return "bir-bir";
	}
	public byte [] postIntercept(byte [] input, boolean isC2S){
		//PythonInterpreter interpreter = new PythonInterpreter();
		byte[] original= input;
		String term= System.getenv("TERM");
		if (term==null) term="linux";
		try{
			byte[] output=new byte[0];
			String buf="";
			String arg="pst";
			if(isC2S) arg+="1";
			else arg+="0";
			arg+=javax.xml.bind.DatatypeConverter.printHexBinary(input);
			Process proc;
			if(!isWindows()){
				PrintWriter writer = new PrintWriter(scripts+ "test", "UTF-8");
				writer.println("export TERM="+term);
				writer.println(pythonpath + " \""+scripts+"main.py\" "+arg);
				writer.close();
				proc = Runtime.getRuntime().exec("sh "+scripts+ "test");
			}
			else {
				proc = Runtime.getRuntime().exec(pythonpath + " \""+scripts+"main.py\" "+arg);
			}


			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				out+=buf+"\n";
				buf=line;

			}
			reader.close();

			reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			line = null;
			while ((line = reader.readLine()) != null)
			{
				err+=line+"\n";
			}
			reader.close();

			proc.destroy();
			//output=fromHexString(buf);
			output=outputRecognise(buf)[0];
			//parseReplies(outputRecognise(buf));
			out=out.substring(1);
			return output;
		}catch(Exception ex){
			ex.printStackTrace();
			return original;
		}

	}

	public byte[][] mangle(byte [] input, boolean isC2S){
		byte[][] original=new byte[1][0];
		original[0]	= input;
		String term= System.getenv("TERM");
		if (term==null) term="linux";
		try{
			byte[][] output=new byte[0][0];
			String buf="";
			String arg="mng";
			if(isC2S) arg+="1";
			else arg+="0";
			arg+=javax.xml.bind.DatatypeConverter.printHexBinary(input);
			Process proc;
			if(!isWindows()){
				PrintWriter writer = new PrintWriter(scripts+ "test", "UTF-8");
				writer.println("export TERM="+term);
				writer.println(pythonpath + " \""+scripts+"main.py\" "+arg);
				writer.close();
				proc = Runtime.getRuntime().exec("sh "+scripts+ "test");
			}
			else {
				proc = Runtime.getRuntime().exec(pythonpath + " \""+scripts+"main.py\" "+arg);
			}


			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				out+=buf+"\n";
				buf=line;

			}
			reader.close();

			reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			line = null;
			while ((line = reader.readLine()) != null)
			{
				err+=line+"\n";
			}
			reader.close();

			proc.destroy();
			//output=fromHexString(buf);
			output=outputRecognise(buf);
			for (int i=1; i< output.length; i++)  output[i]=Arrays.copyOfRange(output[i], 1, output[i].length);
			out=out.substring(1);
			return output;
		}catch(Exception ex){
			ex.printStackTrace();
			return original;
		}
	}


	//Test function
	/*public static void main(String[] args) {
		PythonMangler pm = new PythonMangler();
		byte []out = pm.mangle("test this shit".getBytes(), true);
		System.out.println(new String(out));

	}*/
	private static byte[] fromHexString(final String encoded) {
		if ((encoded.length() % 2) != 0)
			throw new IllegalArgumentException("Input string must contain an even number of characters");

		final byte result[] = new byte[encoded.length()/2];
		final char enc[] = encoded.toCharArray();
		for (int i = 0; i < enc.length; i += 2) {
			StringBuilder curr = new StringBuilder(2);
			curr.append(enc[i]).append(enc[i + 1]);
			result[i/2] = (byte) Integer.parseInt(curr.toString(), 16);
		}
		return result;
	}
	private static byte[][] outputRecognise(final String inp) {
		String[] encoded = inp.split("#");
		int leng= encoded.length;
		byte[][] result = new byte[leng][0];
		for(int i=0; i<leng; i++) result[i]=fromHexString(encoded[i]);
		return result;
	}
	private static boolean isWindows()
	{
		String buf = System.getProperty("os.name");
		buf=buf.toLowerCase();
		if (buf.lastIndexOf("windows") == -1) return false;
		else return true;

	}

	private String getProperties(String key, String defaultValue){
		Properties config = new Properties();
		try {
			//config.load(ClassLoader.getSystemResourceAsStream("NoPE.properties"));
			String path = System.getProperty("user.home");
			File f = new File(path + "/.NoPEProxy/NoPE.properties");
			if(f.exists()){
				config.load( new FileInputStream(f));
			}else{
				//config.load(ClassLoader.getSystemResourceAsStream("NoPE.properties"));
				File p = new File(path + "/.NoPEProxy");
				if(!p.exists())
					p.mkdir();
				f.createNewFile();
			}
			return config.getProperty(key, defaultValue);


		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		return "";

	}

}
