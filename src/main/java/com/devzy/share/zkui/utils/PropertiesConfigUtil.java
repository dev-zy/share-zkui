package com.devzy.share.zkui.utils;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;

/**
  * 项目: emgw-common
  * 描述: Properties文件操作(此性能比较高,支持动态set或get,可持久化文件)
  * 备注: 不支持[classpath:]方式
  * 作者: ZhangYi
  * 时间: 2019年1月16日 下午3:04:00
  * 版本: v1.0
  * JDK: 1.8.192
  * @company 空间畅想
 */
public class PropertiesConfigUtil {
	private static ConcurrentHashMap<String, PropertiesConfiguration> map = new ConcurrentHashMap<String, PropertiesConfiguration>();
	/**
	  * 描述: 加载文件
	  * 作者: ZhangYi
	  * 时间: 2019年1月16日 下午3:20:51
	  * 参数: (参数列表)
	  * @param fileName	文件路径
	  * @return
	 */
	public static PropertiesConfiguration load(String fileName) {
		try {
			PropertiesConfiguration config  = new PropertiesConfiguration(fileName);
			config.setReloadingStrategy(new FileChangedReloadingStrategy());//支持动态获取
			config.setAutoSave(true);//支持动态保存
			if(!map.containsKey(fileName)) {
				map.put(fileName, config);
			}
			return config;
		} catch (ConfigurationException e) {
			e.printStackTrace();// 异常信息
		}
		return null;
	}
	/**
	  * 描述: 获取配置文件
	  * 作者: ZhangYi
	  * 时间: 2019年1月16日 下午3:43:30
	  * 参数: (参数列表)
	  * @param fileName	文件（包含名称和路径）
	  * @return
	 */
	public static PropertiesConfiguration getConfig(String fileName) {
		PropertiesConfiguration config = null;
		if(map!=null&&map.containsKey(fileName)) {
			config = map.get(fileName);
		}else {
			config = load(fileName);
		}
		return config;
	}
	/**
	  * 描述: 修改文件值
	  * 作者: ZhangYi
	  * 时间: 2019年1月16日 下午3:25:54
	  * 参数: (参数列表)
	  * @param key	键
	  * @param value	值
	  * @return
	 */
	public static boolean setProperty(String key,Object value) {
		boolean flag = false;
		if(map!=null&&!map.isEmpty()) {
			for (PropertiesConfiguration config : map.values()) {
				if(config.containsKey(key)) {
					config.setProperty(key, value);
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	/**
	  * 描述: 修改文件值
	  * 作者: ZhangYi
	  * 时间: 2019年1月16日 下午3:25:54
	  * 参数: (参数列表)
	  * @param fileName	指定文件
	  * @param key	键
	  * @param value	值
	  * @return
	 */
	public static boolean setProperty(String fileName,String key,Object value) {
		boolean flag = map.containsKey(fileName);
		if(flag) {
			PropertiesConfiguration config = map.get(fileName);
			if(config.containsKey(key)) {
				config.setProperty(key, value);
				flag = true;
			}else {
				flag = false;
			}
		}
		return flag;
	}
	/**
	  * 描述: 获取属性值
	  * 作者: ZhangYi
	  * 时间: 2019年1月16日 下午3:35:52
	  * 参数: (参数列表)
	  * @param key	键
	  * @return
	 */
	public static Object getProperty(String key) {
		Object value = null;
		if(map!=null&&!map.isEmpty()) {
			for (PropertiesConfiguration config : map.values()) {
				if(config.containsKey(key)) {
					value = config.getProperty(key);
					break;
				}
			}
		}
		return value;
	}
	/**
	 * 描述: 获取指定文件属性值
	 * 作者: ZhangYi
	 * 时间: 2019年1月16日 下午3:35:52
	 * 参数: (参数列表)
	 * @param key	键
	 * @return
	 */
	public static Object getProperty(String fileName,String key) {
		Object value = null;
		if(map!=null&&map.containsKey(fileName)) {
			PropertiesConfiguration config = map.get(fileName);
			value = config.getProperty(key);
		}
		return value;
	}
	/**
	  * 描述: 获取属性值
	  * 作者: ZhangYi
	  * 时间: 2019年1月16日 下午3:35:52
	  * 参数: (参数列表)
	  * @param key	键
	  * @return
	 */
	public static String getString(String key) {
		String value = null;
		if(map!=null&&!map.isEmpty()) {
			for (PropertiesConfiguration config : map.values()) {
				if(config.containsKey(key)) {
					value = config.getString(key);
					if(value!=null&&(value.indexOf("{")<value.indexOf("}")||value.indexOf("${")<value.indexOf("}"))) {
						String reg = value.substring(value.contains("${")?value.indexOf("${"):value.indexOf("{"), value.indexOf("}")+1);
						String rkey = (reg.contains("${")?reg.replace("${", ""):reg.replace("{", "")).replace("}", "");
						String def = System.getProperty(rkey, reg);
						value=value.replace(reg, def);
					}
					break;
				}
			}
		}
		return value;
	}
	/**
	 * 描述: 获取指定文件属性值
	 * 作者: ZhangYi
	 * 时间: 2019年1月16日 下午3:35:52
	 * 参数: (参数列表)
	 * @param key	键
	 * @return
	 */
	public static String getString(String fileName,String key) {
		String value = null;
		if(map!=null&&map.containsKey(fileName)) {
			PropertiesConfiguration config = map.get(fileName);
			value = config.getString(key);
		}
		return value;
	}
	/**
	 * 描述: 获取属性值
	 * 作者: ZhangYi
	 * 时间: 2019年1月16日 下午3:35:52
	 * 参数: (参数列表)
	 * @param key	键
	 * @return
	 */
	public static String getJsonString(String key) {
		String value = null;
		if(map!=null&&!map.isEmpty()) {
			for (PropertiesConfiguration config : map.values()) {
				if(config.containsKey(key)) {
					value = StringUtils.join(config.getList(key), ",");
					break;
				}
			}
		}
		return value;
	}
	/**
	 * 描述: 获取指定文件属性值
	 * 作者: ZhangYi
	 * 时间: 2019年1月16日 下午3:35:52
	 * 参数: (参数列表)
	 * @param key	键
	 * @return
	 */
	public static String getJsonString(String fileName,String key) {
		String value = null;
		if(map!=null&&map.containsKey(fileName)) {
			PropertiesConfiguration config = map.get(fileName);
			value = StringUtils.join(config.getList(key), ",");
		}
		return value;
	}
}
