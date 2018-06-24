package juniter.model.wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import juniter.model.Peer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PeerDoc  implements Serializable{
	
	private static final long serialVersionUID = 1618741167475514278L;
	private List<Peer> peers ;
	
	public transient String url;
	
	public PeerDoc() {
		super();
		peers = new ArrayList<Peer>();
	}
	
	public PeerDoc( List<Peer> peers) {
		super();
		this.peers = peers;
	}

	public List<Peer> getPeers() {
		return peers;
	}
}