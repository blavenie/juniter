package juniter.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import juniter.model.Block;

@Service
public class FileBlocksService {

	private static final Logger log = LogManager.getLogger();

	/**
	 * joiners/identity/certification & parameters
	 */
	Block _0;

	/**
	 * transactions
	 */
	Block _127128;

	/**
	 * leavers
	 */
	Block _102093;

	/**
	 * actives
	 */
	Block _17500;

	/**
	 * revoked/excluded
	 */
	Block _33396;

	@PostConstruct
	public void init() throws IOException {
		log.info("Entering FileBlocksService.init  ");

		ClassLoader cl = this.getClass().getClassLoader();
		ObjectMapper jsonMapper = new ObjectMapper();

		try {
			_0 = jsonMapper.readValue(cl.getResourceAsStream("blocks/0.json"), Block.class);
			log.info("Sucessfully parsed " + _0 + "\tfrom" + cl.getResource("blocks/0.json"));

			_127128 = jsonMapper.readValue(cl.getResourceAsStream("blocks/127128.json"), Block.class);
			log.info("Sucessfully parsed " + _127128 + "\tfrom " + cl.getResource("blocks/127128.json"));

			_102093 = jsonMapper.readValue(cl.getResourceAsStream("blocks/102093.json"), Block.class);
			log.info("Sucessfully parsed " + _102093 + "\tfrom " + cl.getResource("blocks/102093.json"));

			_17500 = jsonMapper.readValue(cl.getResourceAsStream("blocks/17500.json"), Block.class);
			log.info("Sucessfully parsed " + _17500 + "\tfrom " + cl.getResource("blocks/17500.json"));

			_33396 = jsonMapper.readValue(cl.getResourceAsStream("blocks/33396.json"), Block.class);
			log.info("Sucessfully parsed " + _33396 + "\tfrom " + cl.getResource("blocks/33396.json"));

		} catch (Exception e) {
			log.error("Starting FileBlocksService ... " + e);
		}
		log.info("Finished Initializing " + this.getClass().getName());
	}

}