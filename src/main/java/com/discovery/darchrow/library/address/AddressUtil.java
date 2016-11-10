package com.discovery.darchrow.library.address;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.discovery.darchrow.util.ResourceUtil;



/**
 * 快递地址常用工具 通过读取nebula/full-address.json来获取 省市县以及街道的地址信息
 * 
 * nebula/address.json 只保存了省市县的地址信息
 * 
 * @author dongliang ma
 * 
 */
public class AddressUtil {
	private final static Logger log = LoggerFactory
			.getLogger(AddressUtil.class);

	/** 子区域Map key为父区域的Id ,value 为父区域下所有的子区域的列表 **/
	private static Map<Long, List<Address>> subAddressMap = new HashMap<Long, List<Address>>();

	/** 区域Map key为Id , value 为该 Id 代表的区域信息 **/
	private static Map<Long, Address> addressMap = new HashMap<Long, Address>();
	
	/**
	 * 用于保存地址的信息,名称以及idList
	 */
	private static Map<String,List<Address>> addressNameMap=new HashMap<String,List<Address>>();

	/** 包含 省市县，乡镇（街道） 的信息的文件 **/
	private static final String CONFIG_PATH_FULL = "address/full-address.json";

	/** 包含 省市县 的信息的文件 **/
	private static final String CONFIG_PATH = "address/address.json";

	/** 精简后的地址JSON 字符串 **/
	private static String addressJson = null;
	
	private static final Long CHINA_ID = 1L;

	static {
		init();
	}

	/**
	 * 根据pid来获取该pid对应区域下的子区域。 eg: 传入pid 0(代表中国) ,返回 中国的所有省份信息
	 * 
	 * @param pid
	 * @return
	 */
	public static List<Address> getSubAddressByPid(Long pid) {
		return subAddressMap.get(pid);
	}
	
	/**
	 * 根据地址名称获取address list
	 * 
	 * @param pid
	 * @return
	 */
	public static List<Address> getAddressesByName(String name) {
		return addressNameMap.get(name);
	}

	/**
	 * 根据id来获取改id代表的区域信息 eg : id 为310000 ，返回上海的信息
	 * 
	 * @param id
	 * @return
	 */
	public static Address getAddressById(Long id) {
		return addressMap.get(id);
	}
	
	/**
	 * 得到省的地址信息列表
	 */
	public static List<Address> getProviences(){
		return getSubAddressByPid(CHINA_ID);
	}
	
	/**
	 * 得到市的地址信息列表
	 */
	public static List<Address> getCities(Long provienceId){
		return getSubAddressByPid(provienceId);
	}
	
	/**
	 * 得到县的地址信息列表
	 */
	public static List<Address> getCounties(Long cityId){
		return getSubAddressByPid(cityId);
	}
	
	/**
	 * 得到乡/街道的地址信息列表
	 * @param countyId
	 * @return
	 */
	public static List<Address> getTowns(Long countyId){
		return getSubAddressByPid(countyId);
	}

	/**
	 * 生成包含省市区的json字符串（不含乡镇）
	 * 
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static String generateAddressJson() throws JsonGenerationException, JsonMappingException, IOException {

		if (null != addressJson) {
			return addressJson;
		}

		String json = getFileContent(CONFIG_PATH);
		ObjectMapper mapper = new ObjectMapper();

		Map<Long, List<Address>> genSubAddressMap = new HashMap<Long, List<Address>>();

		Map<String, List<String>> map = (Map<String, List<String>>) mapper.readValue(json, Map.class);
		for (String key : map.keySet()) {
			Address address = string2Address(Long.parseLong(key), map.get(key));
			gatherSubAddressMap(genSubAddressMap, address);
		}

		Map<Long, Map<Long, String>> outmap = new HashMap<Long, Map<Long, String>>();

		for (Long pid : genSubAddressMap.keySet()) {
			Map<Long, String> addressMap = new HashMap<Long, String>();

			for (Address address : genSubAddressMap.get(pid)) {
				addressMap.put(address.getId(), address.getName());
			}

			outmap.put(pid, addressMap);
		}

		addressJson = mapper.writeValueAsString(outmap);

		return addressJson;
	}
	
	public static void generateJsFile(String jsPath) throws JsonGenerationException, JsonMappingException, IOException{
		StringBuilder content = new StringBuilder(generateAddressJson());
		content.insert(0,"var districtJson = ");
		FileWriter f=new FileWriter(jsPath);
		f.write(content.toString());
		f.close();
	}

	@SuppressWarnings("unchecked")
	private static void init() {
		String json = getFileContent(CONFIG_PATH_FULL);
		ObjectMapper mapper = new ObjectMapper();

		try {
			Map<String, List<String>> map = (Map<String, List<String>>) mapper
					.readValue(json, Map.class);
			for (String key : map.keySet()) {
				Address address = string2Address(Long.parseLong(key),
						map.get(key));
				gatherSubAddressMap(subAddressMap, address);
				addressMap.put(address.getId(), address);
				
				List<Address> idList=addressNameMap.get(address.getName());
				//如果找不到，则新增
				if(idList==null){	
					idList=new ArrayList<Address>();
					idList.add(address);
					addressNameMap.put(address.getName(), idList);
				}
				//如果找到了，则追加
				else{
					idList.add(address);
				}
			}
		} catch (JsonParseException e) {
			log.error(e.getMessage());
		} catch (JsonMappingException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	private static String getFileContent(String filePath) {
		BufferedReader br = null;
		InputStream in = null;
		try {
			
			in = ResourceUtil.getResourceAsStream(filePath);
			br = new BufferedReader(new InputStreamReader(in,"utf-8"));
			String tmp = null;
			StringBuilder content = new StringBuilder();
			while ((tmp = br.readLine()) != null) {
				content.append(tmp);
			}

			String json = content.toString();

			return json;

		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			return null;
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		} finally {
			try {
				if (null != br) {
					br.close();
				}

				if (null != in) {
					in.close();
				}

			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}

	private static Address string2Address(long id, List<String> info) {
		Address address = new Address();
		address.setId(id);
		address.setName(info.get(0));
		address.setpId(Long.parseLong(info.get(1)));
		address.setSpelling(info.get(2));

		return address;
	}

	private static void gatherSubAddressMap(Map<Long, List<Address>> map,
			Address address) {
		Long pid = address.getpId();
		if (null != map.get(pid)) {
			map.get(pid).add(address);
		} else {
			List<Address> list = new ArrayList<Address>(20);
			list.add(address);
			map.put(pid, list);
		}
	}
	
}
