package home.poolplayer.io;

import java.util.Properties;

/**
 * This class represents a section in an ini file. Each section has a name and
 * the contents are mapped as a key-value pair using java {@link Properties}
 * 
 * @author narasimhan.rajagopal
 * 
 */

public class Section {

	private String name;
	private Properties props;

	public Section() {
		props = new Properties();
	}

	public String getName() {
		return name;
	}

	public Properties getProps() {
		return props;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[" + name + "]");
		buf.append("\n");
		buf.append(props.toString());
		return buf.toString();
	}
}