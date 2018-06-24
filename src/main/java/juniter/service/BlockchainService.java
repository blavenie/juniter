package juniter.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import juniter.model.Block;
import juniter.model.wrapper.WithWrapper;
import juniter.repository.BlockRepository;
import utils.Constants;

/**
 * 
 * Blockchain sub-root of the HTTP API
 * 
 * <pre>
 * |-- blockchain/
 * |   |-- parameters
 * |   |-- membership
 * |   |-- with/
 * |       |-- newcomers
 * |       |-- certs
 * |       |-- joiners
 * |       |-- actives
 * |       |-- leavers
 * |       |-- excluded
 * |       |-- ud
 * |       `-- tx
 * |   |-- hardship
 * |   |   `-- [PUBKEY]
 * |   |-- block
 * |   |   `-- [NUMBER]
 * |   |-- difficulties
 * |   `-- current
 * </pre>
 * 
 * @author ben
 *
 */
@RestController
@RequestMapping("/blockchain")
public class BlockchainService {

	private static final Logger logger = LogManager.getLogger();

	private static final String ERROR_MESSAGE = "<ul>" //
			+ "<li>blockchain/</li>" //
			+ "<ul><li>block/[number]</li>" //
			+ "<li>current</li>" //
			+ "<li>parameters</li>" //
			+ "<ul><li>with</li>" //
			+ "<li>newcomers</li>" //
			+ "<li>certs</li>" //
			+ "<li>joiners</li>" //
			+ "<li>actives</li>" //
			+ "<li>leavers</li>" //
			+ "<li>excluded</li></ul>" //
			+ "</ul></ul>";

	@Autowired
	BlockRepository repository;

	@RequestMapping("/")
	public String index() {
		bulkLoad(Constants.Defaults.BULK_BATCH_SIZE);
		return "Greetings from Spring Boot!<br>" + ERROR_MESSAGE;
	}

	@RequestMapping(value = "/block/{id}", method = RequestMethod.GET)
	public Block block(@PathVariable("id") Integer id) {

		logger.info("Entering /blockchain/block/{number=" + id + "}");
		return repository.findByNumber(id).orElseGet(() -> fetchBlock(id));
	}

	@Transactional
	@RequestMapping(value = "/blocks/{count}/{from}", method = RequestMethod.GET)
	public List<Block> block(@PathVariable("count") Integer count, @PathVariable("from") Integer from) {

		logger.info("Entering /blockchain/blocks/{count=" + count + "}/{from=" + from + "}");

		List<Integer> blocksToFind = IntStream.range(from, from + count).boxed().collect(Collectors.toList());
		logger.info("---blocksToFind: " + blocksToFind);

		List<Block> knownBlocks = repository.findByNumberIn(blocksToFind).collect(Collectors.toList());
		logger.info("---known blocks: " + knownBlocks);

		List<Block> blocksToSave = blocksToFind.stream()
				.filter(b -> !knownBlocks.stream().anyMatch(kb -> kb.getNumber().equals(b))).map(lg -> fetchBlock(lg))
				.collect(Collectors.toList());

		logger.info("---fetch blocks: " + Stream.concat(blocksToSave.stream(), knownBlocks.stream())
				.map(b -> b.getNumber().toString()).collect(Collectors.joining(",")));

		repository.saveAll(blocksToSave);

		return Stream.concat(blocksToSave.stream(), knownBlocks.stream()).collect(Collectors.toList());
	}

	@Transactional
	@RequestMapping(value = "/current", method = RequestMethod.GET)
	public Block current() {
		logger.info("Entering /blockchain/current");

		return repository.findTop1ByOrderByNumberDesc().orElse(fetchBlock("current"));
	}

	@Transactional(readOnly = true)
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public List<Block> all() {

		logger.info("Entering /blockchain/all");

		try (Stream<Block> items = repository.findTop10ByOrderByNumberDesc()) {
			return items.collect(Collectors.toList());
		} catch (Exception e) {
			logger.error(e);
			return null;
		}

	}

	/**
	 * /blockchain/with/{what=[newcomers,certs,actives,leavers,excluded,ud,tx]}
	 * 
	 * <p>
	 * Filters according to 'what' you desire
	 * </p>
	 * <p>
	 * Sort by number
	 * </p>
	 * 
	 * @param what
	 * @return A Wrapped List of Blocks
	 */
	@RequestMapping(value = "/with/{what}", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public WithWrapper with(@PathVariable("what") String what) {

		logger.info("Entering /blockchain/with/{newcomers,certs,actives,leavers,excluded,ud,tx}");
		Stream<Block> st;
		switch (what) {
		case "newcomers":
			st = repository.with(block -> !block.getJoiners().isEmpty());
			break;
		case "certs":
			st = repository.with(block -> !block.getCertifications().isEmpty());
			break;
		case "actives":
			st = repository.with(block -> !block.getActives().isEmpty());
			break;
		case "leavers":
			st = repository.with(block -> !block.getLeavers().isEmpty());
			break;
		case "excluded":
			st = repository.with(block -> !block.getExcluded().isEmpty());
			break;
		case "ud":
			st = repository.with(block -> block.getDividend() != null);
			break;
		case "tx":
		default:
			st = repository.with(block -> !block.getTransactions().isEmpty());
		}

		try (Stream<Integer> items = st.map(b -> b.getNumber())) {
			return new WithWrapper(items.collect(Collectors.toList()));
		} catch (Exception e) {
			logger.error(e);
			return null;
		}

	}

	/**
	 * Wrapper for /blockchain/block/[number]
	 * 
	 * @param number
	 * @return
	 */
	private Block fetchBlock(Integer number) {
		return fetchBlock("block/" + number);
	}

	/**
	 * Fetch a block and save it synchronously
	 * 
	 * @param id the block id
	 */
	@Transactional
	private Block fetchBlock(String id) {
		String url = Constants.Defaults.NODE + "blockchain/" + id;
		logger.info("Fetching block : " + url);
		Block block = null;
		try {
			TimeUnit.MILLISECONDS.sleep(300);
			block = new RestTemplate().getForObject(url, Block.class);
			block = repository.findByNumber(block.getNumber()).orElse(block);
			block = repository.save(block);

			logger.info("... saved block : " + block);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return block;
	}

	@Transactional
	public List<Block> fetchBlocks(String url) {
		try {
			TimeUnit.MILLISECONDS.sleep(300);

			var responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Block>>() {
					});
			var body = responseEntity.getBody();
			var contentType = responseEntity.getHeaders().getContentType().toString();
			var statusCode = responseEntity.getStatusCode().getReasonPhrase();
			
			logger.info("Fetched: " + url + "... Status: " + statusCode + " ContentType: " + contentType);
			return body;

		} catch (InterruptedException e) {
			logger.error(Constants.Logs.INTERRUPTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void bulkLoad(int bulkSize) {
		var lastBLock = current().getNumber();
		var nbBulks = Integer.divideUnsigned(lastBLock, bulkSize);

		var result = IntStream.range(0, nbBulks).map(nbb -> (nbb * bulkSize)).boxed() // get nbBulks Integers with an
																						// offset of bulkSize
				.parallel() // parallelize the stream if needed
				.map(i -> Constants.Defaults.NODE + "/blockchain/blocks/" + bulkSize + "/" + i) // build the url
				.map(url -> fetchBlocks(url)) // Actually fetch the document containing a list of blocks
				.map(list -> repository.saveAll(list)) // persist the collection
				.flatMap(blocks -> blocks.stream()) // put stream as a single collection
				.map(bl -> bl.getNumber()).collect(Collectors.toList());

		logger.info("Bulkloaded " + result.size());
	}
}