package org.vuejs.vue4e.vls;

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import org.json.*;


/**
 * Passes on messages from eclipse to VLS.
 * Does some changes to messages.
 *
 */
public class JsonRpcOutputFilter extends FilterOutputStream  {

	private static final String UTF8 = "UTF-8";
	private static final String CRLF = "\r\n";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length:";

	private JSONObject config = null;
	
	private boolean DEBUG = Boolean.parseBoolean(System.getProperty("vue4e.debug")); //$NON-NLS-1$
	// input
	private StringBuffer in = new StringBuffer();
	int crlf = 0;
	private int length = 0;
	private boolean bHeader = true;
	private int contentLength = -1;

	/**
	 * 
	 * @param out  this is the sink for our data 
	 */
	public JsonRpcOutputFilter(OutputStream out) {
		super(out);
		
		String s;
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("vetur-config.json");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8))) {
			s = br.lines().collect(Collectors.joining(System.lineSeparator()));
			config = new JSONObject(s);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void write(int b) throws IOException {
		//message.append(b);
		parse(b);
	}

	public void write(byte[] b) throws IOException {
		//message.append(b);
		for(byte ch : b) {
			parse(ch);
		}
	}

	public void write(byte[] b, int off, int len) throws IOException {
		//message.append(b, off, len);
		for(int i = off; i < off+len; i ++) {
			parse(b[i]);
		}
	}

	/*
	public InputStream getInputStream() {
		return new InputStream() {

			@Override
			public int read() throws IOException {
				if(outPos >= out.length()) {
					if(outPos > 0) {
						out.delete(0, out.length());
						outPos = 0;
					}
					return -1;
				}
				else {
					int ch = out.codePointAt(outPos);
					outPos ++;
					return ch;
				}
			}

		};
	}
*/	

	private void parse(int ch) {
		
		if(bHeader) {
			in.append((char)ch);
			if(ch == '\r') {
				crlf = 1;
			}
			else if(ch == '\n') {
				if(crlf == 1) {
					// line end
					String s = in.toString();
					in.delete(0, in.length());
					if(s.toLowerCase().startsWith(CONTENT_LENGTH_HEADER.toLowerCase())) {
						contentLength = Integer.parseInt(s.substring(CONTENT_LENGTH_HEADER.length()).trim());
					}
					else {
						// unknown header. append to output
						if(s.trim().isEmpty()) {
							// we should end the headers once we see an empty header (CRLF only)
							bHeader = false;
						}
						else {
							write(s);
						}
					}
				}
				else {
					crlf = 0;
				}
				
			}
			else {
			}
		}
		else if(!bHeader) {
			in.append((char)ch);
			length ++;
			if(length >= contentLength) {
				parseContent();
				in.delete(0, in.length());
				bHeader = true;
				contentLength = -1;
				length = 0;
			}

		}
				
	}
	
	
	private void write(String s) {
		try {
			if(DEBUG) {
				System.err.print(s);
			}
			out.write(s.getBytes(Charset.forName(UTF8)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * parses the content provided in {in} and outputs to {out} starting with new content length
	 */
	private void parseContent() {

		if(DEBUG) {
			System.out.println("Before Parse:  " + in.toString());
		}

		JSONObject o = new JSONObject(in.toString());
		
		String method = o.getString("method");
		if(method.equals("initialize")) {
			// method=initialize --> params.initializationOptions.config must be given
			// Since vetur likes to run in a VSCode environment we must provide it with some default config options
			// that are always present in VSCode. 
			JSONObject params = o.getJSONObject("params");
			if(params.opt("initializationOptions") == null) {
				params.put("initializationOptions", new JSONObject());
			}
			params.getJSONObject("initializationOptions").put("config", config);
			
			//params.put("clientInfo", new JSONObject("{\"name\":\"vue4e\", \"version\":\"0.1.0\"}"));
		}

		// TODO: handle errors & restart server

		// {"jsonrpc":"2.0","id":"51","error":{"code":-32603,"message":"Request shutdown failed with message: Cannot read property 'languageServiceRefCount' of undefined"}}Content-Length: 47

		
		String s = o.toString();
		if(DEBUG) {
			System.out.println("After Parse:  " + s);
		}
		
		write(CONTENT_LENGTH_HEADER + " " + Integer.toString(s.length()) + CRLF);
		write(CRLF);
		write(s);
	
	}
	
}
