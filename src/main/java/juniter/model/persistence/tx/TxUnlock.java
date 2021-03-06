package juniter.model.persistence.tx;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Embeddable
public class TxUnlock implements Serializable {

	private static final long serialVersionUID = -2759081749575814229L;

	private static final Logger logger = LogManager.getLogger();
	
	private Integer id;

	private String function; 
	
	public Integer Id() {
		return id;
	}

	public String Function() {
		return function;
	}
	
	public int functionReference() {
		return Integer.parseInt(function.substring(4,function.length()-1));
	}

	public TxUnlock() {
	}
	
	public TxUnlock(String unlock) {
		setUnlock(unlock);
	}
	
	public String getUnlock(){
		return id +":"+function;
	}
	
	public void setUnlock(String unlock) {
		
		logger.debug("Parsing TxUnlock... "+unlock);
		
		var vals = unlock.split(":");
		id = Integer.valueOf(vals[0]);
		function = vals[1];
	}
	
	public enum InputFct {
		SIG("SIG"), XHX("XHX");

		private final String FCT_TYPE;

		InputFct(String transactionType) {
			this.FCT_TYPE = transactionType;
		}

		public String getEndPointType() {
			return this.FCT_TYPE;
		}

	}
	
}
