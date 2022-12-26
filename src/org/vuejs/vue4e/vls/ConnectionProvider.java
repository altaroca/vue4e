package org.vuejs.vue4e.vls;


import java.io.*;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.vuejs.vue4e.Vue4ePlugin;

// see https://www.eclipse.org/community/eclipse_newsletter/2017/may/article3.php

public class ConnectionProvider implements StreamConnectionProvider {

	private boolean DEBUG = Boolean.parseBoolean(System.getProperty("vue4e.debug")); //$NON-NLS-1$

	private final  static String NPM_COMMAND = "npm";
	protected final static String SERVER_PATH = "/server";
	protected final  static String NODE_MODULES_PATH = "/node_modules";
	// vetur:
  // private final static String VLS_NODE_MODULE = "vls";  // OLD "vue-language-server";
  // private final static String VLS_LOCATION = "/" + VLS_NODE_MODULE + "/bin";
	// private final static String VLS_COMMAND = "vls";
  // volar: 
  private final static String VLS_NODE_MODULE = "@volar/vue-language-server";
  private final static String VLS_LOCATION = "/" + VLS_NODE_MODULE + "/bin";
  private final static String VLS_COMMAND = "vue-language-server.js";
  private final static String VLS_ARGUMENTS = "--stdio";
	private final static String WIN_CMD_EXTENSION = ".cmd";  // extension .cmd only needed for Windows
	private final static String INPUT_PREFIX = "[IN]  ";
	private final static String OUTPUT_PREFIX = "[OUT] ";
	
	// BUG:  vue-language-server does not work any more (missing dependency)
	//       and our JsonRPC parser does not work with the newer vls (scrambled text)
	//       and vls gives error "Unhandled method textDocument/documentColor"
	
	private VlsServer server = null;

	public ConnectionProvider() {
	}

	@Override
	public void start() throws IOException {
		
		Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
				Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
				"Starting VLS connection provider"));
		
		this.server = new VlsServer();
		this.server.start();
		
		// wait for the server to start or to fail. 
		// TODO: Timeout ??
		while(this.server.isStarting()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		if(!this.server.isRunning()) {
			
			try {
				this.server.join();
			} catch (InterruptedException e) {
			}
			this.server = null;
			throw new IOException("Could not start VLS");
		}
		else {
			Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
					Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
					"VLS server started successfully "));
		}
	}

	@Override
	public void stop() {
		Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
				Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
				"Stopping VLS connection provider"));

		if ((server != null) && server.isRunning()) {
			server.interrupt();
			try {
				server.join();
			} catch (InterruptedException e) {
				// just exit
			}
			server = null;
		}
	}
	
	/**
	 * server to eclipse
	 */
	@Override
	public InputStream getInputStream() {
		if(server == null) return null;
		InputStream is = 
				server.getInputStream();
		if (DEBUG) {
			return new FilterInputStream(is) {
				@Override
				public int read() throws IOException {
					int res = in.read();
					System.err.print((char) res);
					return res;
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					int bytes = in.read(b, off, len);
					byte[] payload = new byte[bytes];
					System.arraycopy(b, off, payload, 0, bytes);
					System.err.print(INPUT_PREFIX + new String(payload));
					return bytes;
				}

				@Override
				public int read(byte[] b) throws IOException {
					int bytes = in.read(b);
					byte[] payload = new byte[bytes];
					System.arraycopy(b, 0, payload, 0, bytes);
					System.err.print(INPUT_PREFIX + new String(payload));
					return bytes;
				}
			};
		} else {
			return is;
		}
	}

	/**
	 * eclipse to server
	 */
	@Override
	public OutputStream getOutputStream() {
		if(server == null) return null;
		OutputStream os = new JsonRpcOutputFilter(server.getOutputStream());
		if (DEBUG) {
			return new FilterOutputStream(os) {
				
				@Override
				public void write(int b) throws IOException {
					System.err.print((char) b);
					out.write(b);
				}
		
				@Override
				public void write(byte[] b) throws IOException {
					System.err.print(OUTPUT_PREFIX + new String(b));
					out.write(b);
				}
		
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					byte[] actual = new byte[len];
					System.arraycopy(b, off, actual, 0, len);
					System.err.print(OUTPUT_PREFIX + new String(actual));
					out.write(b, off, len);
				}
			};
		}
		else {
			return os;
		}
	}

	@Override
	public InputStream getErrorStream() {
		if(server == null) return null;
		return server.getErrorStream();
	}

	@Override
  public InputStream forwardCopyTo(InputStream input, OutputStream output) {
	  if (input == null)
      return null;
    if (output == null)
      return input;

    if (DEBUG) {
      FilterInputStream filterInput = new FilterInputStream(input) {
        @Override
        public int read() throws IOException {
          int res = in.read();
          System.err.print((char) res);
          return res;
        }
  
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
          int bytes = in.read(b, off, len);
          byte[] payload = new byte[bytes];
          System.arraycopy(b, off, payload, 0, bytes);
          output.write(payload, 0, payload.length);
          System.err.print(OUTPUT_PREFIX + new String(payload));
          return bytes;
        }
  
        @Override
        public int read(byte[] b) throws IOException {
          int bytes = in.read(b);
          byte[] payload = new byte[bytes];
          System.arraycopy(b, 0, payload, 0, bytes);
          output.write(payload, 0, payload.length);
          System.err.print(OUTPUT_PREFIX + new String(payload));
          return bytes;
        }
      };
  
      return filterInput;
	  }
	  else {
	    return StreamConnectionProvider.super.forwardCopyTo(input, output);
	  }
  }

  @Override
  public Object getInitializationOptions(URI rootUri) {
    // TODO Auto-generated method stub
    return StreamConnectionProvider.super.getInitializationOptions(rootUri);
  }

  
  /**
   * Provides trace level to be set on language server initialization.<br>
   * Legal values: "off" | "messages" | "verbose".
   *
   * @param rootUri
   *            the workspace root URI.
   *
   * @return the initial trace level to set
   * @see "https://microsoft.github.io/language-server-protocol/specification#initialize"
   */
  @Override
  public String getTrace(URI rootUri) {
    // TODO Auto-generated method stub
    return StreamConnectionProvider.super.getTrace(rootUri);
  }



  private class VlsServer extends Thread {
		
		// these variables are written by one thread and read by another --> volatile
		volatile private Process process = null;
		volatile private boolean bIsStarting = false;
		
		/**
		 * Returns the output stream connected to the normal input of the server. 
		 * Output to the stream is piped into the standard input of the server 
		 * represented by this object.
		 * 
		 */
		public OutputStream getOutputStream() {
			if(process == null) return null;
			return process.getOutputStream();
		}

		public InputStream getInputStream() {
			if(process == null) return null;
			return process.getInputStream();
		}

		public InputStream getErrorStream() {
			if(process == null) return null;
			return process.getErrorStream();
		}
	
		public boolean isStarting() {
			return this.bIsStarting;
		}

		synchronized public boolean isRunning() {
			return (this.process != null); // && !isStarting();
		}

		public void start() {
			this.bIsStarting = true;
			super.start();
		}
		
    public void run() {

	  	// check for a running server that should be terminated first?
	    		
    	String vlsCmd = System.getenv("");
    	if(vlsCmd == null) {
    		try {
					vlsCmd = Vue4ePlugin.getDefault().getPluginDir()
								.append(SERVER_PATH + NODE_MODULES_PATH + VLS_LOCATION + "/" + VLS_COMMAND + " " + VLS_ARGUMENTS)
								.toOSString();
	    			if(Vue4ePlugin.isWindowsPlatform() && !VLS_COMMAND.contains(".")) {
	    				vlsCmd += WIN_CMD_EXTENSION;
	    			}
				} catch (IOException e) {
					e.printStackTrace();
					this.bIsStarting = false;
					return;
				}
    	}
    		
    	boolean bTryToInstall = true;
	    	
    	while(process == null) {
    		Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
    				Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
    				"Starting VLS server"));

	    	try {
	    		process = Runtime.getRuntime().exec(vlsCmd);
	    		// getOutputStream(), getInputStream(), and getErrorStream()
	    		// waitFor
	    	}
	    	catch(Throwable e) {
	    		Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
	    				Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
	    				e.getLocalizedMessage()));
	    		process = null;
	    	}
	    		
	    	if(process == null) {
	    			if(!bTryToInstall) {
	    				break;
	    			}
	    			bTryToInstall = false; // only try once
	    			
	        		String npmCmd = System.getenv(""); //$NON-NLS-1$
	        		if(npmCmd == null) {
	        			npmCmd = NPM_COMMAND;
	        			if(Vue4ePlugin.isWindowsPlatform()) {
	        				npmCmd += WIN_CMD_EXTENSION;
	        			}
	        		}

	    			Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
	    					Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
	    					"Installing VLS server"));

	    			// global install needs 'sudo' etc on unix --> install into workspace
	    			
	    			IPath pathServer;
	    			try {
	    				// mkdir -p {workspace}/.metadata/{plugin-id}/server/node_modules
						  Vue4ePlugin.getDefault().getPluginDir(SERVER_PATH + NODE_MODULES_PATH);
						  pathServer = Vue4ePlugin.getDefault().getPluginDir(SERVER_PATH);
					  } catch (IOException e1) {
						  e1.printStackTrace();
						  break;
					  }
	    			
	    			try {
	    				process = Runtime.getRuntime().exec(npmCmd +
	    						" install --prefix " + pathServer.toOSString() + " " + VLS_NODE_MODULE);
	    				process.waitFor();
	        			//printProcessOutput();
	    				process = null;
	    			}
	    			catch(Throwable e) {
	    				// cannot run or install VLS. There is nothing else we can do here.
	    				e.printStackTrace();
	    				process = null;
	    				break;
	    			}
	    		}
    		}
    		// we are finished starting. either the server is running or we have failed.
    		this.bIsStarting = false;
    		if(process != null) {
    			boolean bTerminated = false;

    			Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
    					Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
    					"VLS server is running (process " + process.hashCode() + ")."));
    			
    			while (!interrupted() && !bTerminated) {
    			  try {
						  bTerminated = process.waitFor(200, TimeUnit.MILLISECONDS);
		    		  printInputStream(getErrorStream());
					  } catch (Throwable e) {
		          Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
		    			  Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
		    			  e.getLocalizedMessage()));
		    		  e.printStackTrace();
						  // fall out of while loop
		    	    bTerminated = true;
					  }
	        }
    			Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
    					Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
    					"VLS server has exited (process " + process.hashCode() + ")."));
    			
    			printInputStream(getErrorStream());
    			printInputStream(getInputStream());
    			process = null;
    		}
    	}
	    
	    private void printInputStream(InputStream is) {
  			ByteArrayOutputStream sb = new ByteArrayOutputStream();
  			byte[] buffer = new byte[255];
  			int n;
  			try {
    			while((n = is.available()) > 0) {
    				n = is.read(buffer, 0, Math.min(n, buffer.length));
    				if(n < 0) break;
   					sb.write(buffer, 0, n);
    			}
    			if(sb.size() > 0) {
        			Vue4ePlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
        					Vue4ePlugin.getDefault().getBundle().getSymbolicName(),
        					sb.toString("UTF-8")));
    			}
  			} catch (IOException e) {
  				e.printStackTrace();
  			}
	    }
	}
}
