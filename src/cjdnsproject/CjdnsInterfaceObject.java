package cjDNSInterface;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class CjdnsInterfaceObject {

	private String path; // Path to the cjdns config file
	private File file; // the actual config file at $path
	private String user; // the user name for the proposed user
	private StringBuffer fileContents;
	private String passKey; // the unique key ID generated for the user
	private ArrayList<String> arrayListLines; // array to store the strings
	
	public CjdnsInterfaceObject(String path, String user) {
		this.path = path;
		this.file = new File(this.path);
		this.user = user;
		
	}
	
	StringBuffer accessFile() { 
		// returns the text contained in the config file as a
		// StringBuffer object
		String line;
		fileContents = new StringBuffer();
		try { 
			// BufferedReader to read the contents of the file into memory
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			while ((line = reader.readLine()) != null) {
				fileContents.append(line + "\n");
			}
			reader.close();
			
		}
		catch (IOException i) {
			System.err.println("FILE NOT FOUND!");
		}
		return fileContents; // fileContents is the stringbuffer that holds the text of the config file
	}
		
	int locateInsertionPoint() {
		arrayListLines = new ArrayList<String>();
		String[] fileContentsLine;
		
		// split the StringBuffer into an array with each line as an element
		fileContentsLine = fileContents.toString().split("\n"); 
		System.out.println("Testing # of lines check: " + fileContentsLine.length); 
		
		for(int i=0;i<fileContentsLine.length;i++) {
			arrayListLines.add(fileContentsLine[i]);
		}
		System.out.println("Array List # Lines Check: " + arrayListLines.size());
		
		passKey = this.generateUniquePasskey();
		System.out.println("Passkey: " + passKey);
		
		if (this.validatePassKey(passKey, arrayListLines)) {
			int insertIndex = this.getInsertIndex(arrayListLines);
			return insertIndex;
		}
		return -1;
	}

	private int getInsertIndex(ArrayList<String> a) {
		int insertionPoint = 8;
		for(int i=0;i<a.size();i++) {
			if (a.get(i).contains("authorizedPasswords")) {
				System.out.printf("%s is located at index %d%n", a.get(i), i);
				System.out.printf("%s is located at index %d%n", a.get(i+insertionPoint), i+insertionPoint);
				return i+insertionPoint;
			}
		}
		
		return 0;
	}

	private boolean validatePassKey(String key, ArrayList<String> a) {
		for (int i=0;i<a.size();i++) {
			if (a.get(i).contains(key)) {
				System.err.println("KEY EXISTS!");
				return false;
			}
		}
		System.out.println("Key is validated!");
		return true;
	}

	private String generateUniquePasskey() {
		SecureRandom random = new SecureRandom();
		
		return new BigInteger(155, random).toString(32);
	}

	public void insertEntry(int insertIndex) throws IOException {
		// inserts the new username and passkey into the string array
		// converts it to a string, and then saves over the file
		
		DateFormat df = new SimpleDateFormat("MM/DD/YYYY HH:mm");
		Date date = new Date();
		String entry = "\t   {\"password\": \"" + passKey + "\", \"user\": \"" + user + "\"}, \\\\ User added by "+
		System.getProperty("user.name") + " at " + df.format(date) + "\n";
		//System.out.println(entry + " at " + insertIndex);
		arrayListLines.add(insertIndex, entry);
		
		File tmpFile = new File("cjdns_tmp_file.conf");
		if (tmpFile.exists()) {
			tmpFile.delete();
			tmpFile.createNewFile();
		}
		else {
			tmpFile.createNewFile();
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(tmpFile));

		
		for (int i=0;i<arrayListLines.size();i++) {
		output.append(arrayListLines.get(i)+"\n");
		}
		
		
		System.out.println("SUCCESS: " + tmpFile.getAbsolutePath());
		output.close();

		
	}
}
