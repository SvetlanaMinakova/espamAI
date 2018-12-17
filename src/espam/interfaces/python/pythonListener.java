package espam.interfaces.python;
import java.io.*;
import java.util.concurrent.TimeUnit;

/** Class, calls python script and waits for the result */
public class pythonListener extends Thread {

    /**
     * Create new named thread with specified input stream
     * @param name name of the thread
     * @param inputStream input stream specification
     */
    public pythonListener(String name, InputStream inputStream){
         setName(name);
         _bfr = new BufferedReader(new InputStreamReader(inputStream));
         _pythonScriptResult = "";
         _timeout = 0;
    }

	@Override
	public void run() {
		do {
			if (!Thread.interrupted()) {
				try {
					String line;
					while ((line = _bfr.readLine()) != null) {
						_pythonScriptResult += line;
					}
					if (_pythonScriptResult != "") {
						return;
					}

				} catch (IOException ioe) {
					System.err.println("Python listener IO exception");
					return;
				}

				try {
					TimeUnit.SECONDS.sleep(1);
					_timeout++;
					if(_timeout>_maxTimeout)
						throw new InterruptedException();

				}

				catch (InterruptedException e) {
					return;
				}
			}
		}
			while (true) ;
	}


	/**
	 * Return result of the python script
	 * @return result of the python script
	 */
    public synchronized String returnResult(){
		return _pythonScriptResult;
	}

	/** console buffer reader*/
	private BufferedReader _bfr;

	/** result of the python script*/
	private String _pythonScriptResult;

	/** thread start time in seconds*/
	private int _timeout;

	/** max timeout in seconds*/
	private int _maxTimeout = 3600;
}
