package juniter.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import juniter.model.enums.TxType;
import juniter.utils.Constants;

//@JsonIgnoreProperties(ignoreUnknown = true)
@Embeddable
public class TxInput implements Serializable {

	private static final long serialVersionUID = 860920319125591515L;

	private static final Logger logger = LogManager.getLogger();

	// private String input;

	private Integer amount;

	private Integer base;

	@Enumerated(EnumType.STRING)
	@Column(length = 1)
	private TxType type;

	@Valid
	@AttributeOverride(name = "pubkey", column = @Column(name = "dsource"))
	private PubKey dsource = new PubKey();

	private Integer dBlockID;

	@Pattern(regexp = Constants.Regex.HASH)
	private String tHash;

	private Integer tIndex;

	public TxInput() {
	}

	public TxInput(String input) {
		setInput(input);
	}

	public String getInput() {
		return amount + ":" + base + ":" + type + ":"
				+ (TxType.D.equals(type) ? (dsource + ":" + dBlockID) : (tHash + ":" + tIndex));
	}


	public void setInput(String input) {
		logger.info("parse TxInput ... "+input);
		//this.input = input;
		var it = input.split(":");
		amount = Integer.valueOf(it[0]);
		base = Integer.valueOf(it[1]);
		setType(TxType.valueOf(it[2]));

		if (type.equals(TxType.T)) {
			tHash = it[3];
			tIndex = Integer.valueOf(it[4]);
		}

		if (type.equals(TxType.D)) {
			dsource.setPubkey(it[3]);
			dBlockID = Integer.valueOf(it[4]);
		}
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public void setBase(Integer base) {
		this.base = base;
	}

	public void setType(TxType txType) {
		this.type = txType;
	}

	public void setdSource(PubKey dSource) {
		this.dsource = dSource;
	}

	public void setdBlockID(Integer dBlockID) {
		this.dBlockID = dBlockID;
	}

	public void settHash(String tHash) {
		this.tHash = tHash;
	}

	public void settIndex(Integer tIndex) {
		this.tIndex = tIndex;
	}

}