package home.poolplayer.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses a file that conforms to the INI format outlined below
 * <p>
 * <i> ;comment <br>
 * [section 1] <br>
 * key1=value1 <br>
 * key2=value2 <br>
 * ... <br>
 * ... <br>
 * [section 2] <br>
 * key1=value1 <br>
 * key2=value2 <br>
 * ... <br>
 * ... <br>
 * </i>
 * </p>
 * 
 * @author narasimhan.rajagopal
 * 
 */

public class ConfigFileReader {

	private List<Section> sections;
	private String fname;
	private char comment;

	/**
	 * Parses the given ini file and populates sections
	 * 
	 * @param fname
	 *            The name of the file to be parsed
	 * @param comment
	 *            The comment string
	 */

	public ConfigFileReader(String fname, char comment) {
		fname = fname.replace("\\", File.separator);
		this.comment = comment;
		this.fname = fname;
		sections = new ArrayList<Section>();
		read();
	}

	/**
	 * Parses the given ini file and populates sections. The comment character
	 * used will be ';'
	 * 
	 * @param fname
	 *            The name of the file to be parsed
	 */

	public ConfigFileReader(String fname) {
		fname = fname.replace("\\", File.separator);
		this.fname = fname;
		this.comment = ';';
		sections = new ArrayList<Section>();
		read();
	}

	private void read() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fname));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() < 3)
					continue;

				if (line.charAt(0) == comment)
					continue;

				if (line.charAt(0) == '['
						&& line.charAt(line.length() - 1) == ']') {
					Section sec = new Section();
					sec.setName(line.substring(1, line.length() - 1));

					try {
						parseSection(reader, sec);
					} catch (Exception e) {
						System.err.println("Cannot read section "
								+ sec.getName());
					}
					sections.add(sec);
				}
			}
		} catch (Exception e) {
			System.err.println("Cannot read config file " + fname);
		}
	}

	private void parseSection(BufferedReader reader, Section sec)
			throws Exception {
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;

			if (line.charAt(0) == comment)
				continue;

			// Beginning of next section, reset the reader to the last marked
			// position and return
			if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
				reader.reset();
				return;
			}

			String[] strs = line.split("=");
			if (strs == null || strs.length != 2) {
				System.err.println("Invalid line: " + line);
				continue;
			} else {
				sec.getProps().put(strs[0].trim(), strs[1].trim());

				// Successfully read the line, mark it
				reader.mark(0);
			}
		}
	}

	public List<Section> getSections() {
		return sections;
	}

	public char getComment() {
		return comment;
	}

	public String getFileName() {
		return fname;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (Section s : sections)
			buf.append(s.toString() + "\n");

		return buf.toString();
	}

	public static void main(String[] args) throws Exception {
		String fname = "C:\\Narsi\\Everest\\For Learning\\simulation\\simulation.config";
		ConfigFileReader reader = new ConfigFileReader(fname, ';');
		System.out.println(reader.toString());
	}
}