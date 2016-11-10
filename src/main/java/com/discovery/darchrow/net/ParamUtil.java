/*
 * Copyright (C) 2008 feilong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.discovery.darchrow.net;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.discovery.darchrow.bean.ConvertUtil;
import com.discovery.darchrow.lang.CharsetType;
import com.discovery.darchrow.lang.StringUtil;
import com.discovery.darchrow.util.MapUtil;
import com.discovery.darchrow.util.Validator;


/**
 * 处理参数相关.
 * 
 * @author 金鑫 2010-4-15 下午04:01:29
 */
public final class ParamUtil{

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamUtil.class);

    /** Don't let anyone instantiate this class. */
    private ParamUtil(){
        //AssertionError不是必须的. 但它可以避免不小心在类的内部调用构造器. 保证该类在任何情况下都不会被实例化.
        //see 《Effective Java》 2nd
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    /**
     * 转成自然排序的字符串,生成待签名的字符串. <br>
     * 
     * <ol>
     * <li>对数组里的每一个值从 a 到 z 的顺序排序，若遇到相同首字母，则看第二个字母， 以此类推。</li>
     * <li>排序完成之后，再把所有数组值以“&”字符连接起来</li>
     * <li>没有值的参数无需传递，也无需包含到待签名数据中.</li>
     * <li><span style="color:red">注意: 待签名数据应该是原生值而不是 encoding 之后的值</span></li>
     * </ol>
     * 
     * <h3>代码流程:</h3> <blockquote>
     * <ol>
     * <li>{@code if isNullOrEmpty(filePath)---->} return {@link org.apache.commons.lang3.StringUtils#EMPTY}</li>
     * <li>paramsMap to naturalOrderingMap(TreeMap)</li>
     * <li>for naturalOrderingMap's entrySet(),join key and value use =,join each entry use &</li>
     * </ol>
     * </blockquote>
     *
     * @param paramsMap
     *            用于拼接签名的参数
     * @return the string
     * @since 1.2.0
     */
    public static String toNaturalOrderingString(Map<String, String> paramsMap){
        if (Validator.isNullOrEmpty(paramsMap)){
            return StringUtils.EMPTY;
        }

        StringBuilder sb = new StringBuilder();

        Map<String, String> naturalOrderingMap = new TreeMap<String, String>(paramsMap);

        int i = 0;
        int size = naturalOrderingMap.size();
        for (Map.Entry<String, String> entry : naturalOrderingMap.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();

            sb.append(key + "=" + value);

            // 最后一个不要拼接 &
            if (i != size - 1){
                sb.append("&");
            }
            ++i;
        }

        String naturalOrderingString = sb.toString();
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug(naturalOrderingString);
        }
        return naturalOrderingString;
    }

    // ************************************addParameter******************************************************

    /**
     * 添加参数 加入含有该参数会替换掉.
     * 
     * @param url
     *            the url
     * @param paramName
     *            添加的参数名称
     * @param parameValue
     *            添加的参数值
     * @param charsetType
     *            the charset type
     * @return 添加参数 加入含有该参数会替换掉
     */
    public static String addParameter(String url,String paramName,Object parameValue,String charsetType){
        URI uri = URIUtil.create(url, charsetType);
        return addParameter(uri, paramName, parameValue, charsetType);
    }
    
    /**
     * 添加参数,如果uri包含指定的参数名字,那么会被新的值替换.
     * 
     * <h3>示例1:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * String beforeUrl = "www.baidu.com";
     * Map{@code <String, String>} keyAndArrayMap = new LinkedHashMap{@code <String, String>}();
     * 
     * keyAndArrayMap.put("province", "江苏省");
     * keyAndArrayMap.put("city", "南通市");
     * 
     * LOGGER.info(ParamUtil.addParameterSingleValueMap(beforeUrl, keyAndArrayMap, CharsetType.UTF8));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * {@code www.baidu.com?province=%E6%B1%9F%E8%8B%8F%E7%9C%81&city=%E5%8D%97%E9%80%9A%E5%B8%82}
     * </pre>
     * 
     * </blockquote>
     * <h3>示例2:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * String beforeUrl = "www.baidu.com?a=b";
     * Map{@code <String, String>} keyAndArrayMap = new LinkedHashMap{@code <String, String>}();
     * 
     * keyAndArrayMap.put("province", "江苏省");
     * keyAndArrayMap.put("city", "南通市");
     * 
     * LOGGER.info(ParamUtil.addParameterSingleValueMap(beforeUrl, keyAndArrayMap, CharsetType.UTF8));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * {@code www.baidu.com?a=b&province=%E6%B1%9F%E8%8B%8F%E7%9C%81&city=%E5%8D%97%E9%80%9A%E5%B8%82}
     * </pre>
     * 
     * </blockquote>
     *
     * @param uriString
     *            the uri string
     * @param singleValueMap
     *            singleValueMap param name 和value 的键值对
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>uriString</code> 是null或者empty,返回 {@link StringUtils#EMPTY}<br>
     * @see #addParameterArrayValueMap(String, Map, String)
     */
    public static String addParameterSingleValueMap(String uriString,Map<String, String> singleValueMap,String charsetType){
        return addParameterArrayValueMap(uriString, MapUtil.toArrayValueMap(singleValueMap), charsetType);
    }
    
    /**
     * 添加参数,如果uri包含指定的参数名字,那么会被新的值替换.
     * 
     * <p>
     * 如果 解析的<code>queryString</code> 不为空,那么会解析成map,此后再拼接 <code>arrayValueMap</code>;<br>
     * 内部使用 {@link LinkedHashMap},保持map元素顺序
     * </p>
     * 
     * <h3>示例1:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * String beforeUrl = "www.baidu.com";
     * Map{@code <String, String[]>} keyAndArrayMap = new LinkedHashMap{@code <String, String[]>}();
     * 
     * keyAndArrayMap.put("receiver", new String[] { "鑫哥", "feilong" });
     * keyAndArrayMap.put("province", new String[] { "江苏省" });
     * keyAndArrayMap.put("city", new String[] { "南通市" });
     * LOGGER.info(ParamUtil.addParameterArrayValueMap(beforeUrl, keyAndArrayMap, CharsetType.UTF8));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * {@code www.baidu.com?receiver=%E9%91%AB%E5%93%A5&receiver=feilong&province=%E6%B1%9F%E8%8B%8F%E7%9C%81&city=%E5%8D%97%E9%80%9A%E5%B8%82}
     * </pre>
     * 
     * </blockquote>
     * 
     * 
     * <h3>示例2:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * String beforeUrl = "www.baidu.com?a=b";
     * Map{@code <String, String[]>} keyAndArrayMap = new LinkedHashMap{@code <String, String[]>}();
     * keyAndArrayMap.put("province", new String[] { "江苏省" });
     * keyAndArrayMap.put("city", new String[] { "南通市" });
     * LOGGER.info(ParamUtil.addParameterArrayValueMap(beforeUrl, keyAndArrayMap, CharsetType.UTF8));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * {@code www.baidu.com?a=b&province=%E6%B1%9F%E8%8B%8F%E7%9C%81&city=%E5%8D%97%E9%80%9A%E5%B8%82}
     * </pre>
     * 
     * </blockquote>
     *
     * @param uriString
     *            the uri string
     * @param arrayValueMap
     *            the name and array value map
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 添加参数,如果uri包含指定的参数名字,那么会被新的值替换<br>
     *         如果 <code>uriString</code> 是null或者empty,返回 {@link StringUtils#EMPTY}<br>
     *         如果 <code>arrayValueMap</code> 是null或者empty,直接返回 <code>uriString</code><br>
     * @see #addParameterArrayValueMap(URI, Map, String)
     * @since 1.4.0
     */
    public static String addParameterArrayValueMap(String uriString,Map<String, String[]> arrayValueMap,String charsetType){
        return addParameterArrayValueMap(uriString, URIUtil.getQueryString(uriString), arrayValueMap, charsetType);
    }

    /**
     * 添加参数.
     * 
     * <p>
     * 如果uri包含指定的参数名字,那么会被新的值替换,比如原来是{@code a=1&a=2},现在使用a,[3,4]调用这个方法,会返回{@code a=3&a=4}.
     * </p>
     * 
     * @param uri
     *            如果带有? 和参数,会先被截取,最后再拼接,<br>
     *            如果不带?,则自动 增加?
     * @param arrayValueMap
     *            singleValueMap 类似于 request.getParameterMap
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>uri</code> 是null,返回 {@link StringUtils#EMPTY}<br>
     */
    public static String addParameterArrayValueMap(URI uri,Map<String, String[]> arrayValueMap,String charsetType){
        return null == uri ? StringUtils.EMPTY : addParameterArrayValueMap(uri.toString(), uri.getRawQuery(), arrayValueMap, charsetType);
    }

    /**
     * 添加参数 加入含有该参数会替换掉.
     * 
     * @param url
     *            the url
     * @param nameAndValuesMap
     *            nameAndValueMap param name 和value 的键值对
     * @param charsetType
     *            the charset type
     * @return 添加参数 加入含有该参数会替换掉
     */
    public static String addParameterArrayMap(String url,Map<String, String[]> nameAndValuesMap,String charsetType){
        URI uri = URIUtil.create(url, charsetType);
        return addParameterArrayMap(uri, nameAndValuesMap, charsetType);
    }

    /**
     * 添加参数 加入含有该参数会替换掉.
     * 
     * @param url
     *            the url
     * @param nameAndValueMap
     *            nameAndValueMap param name 和value 的键值对
     * @param charsetType
     *            the charset type
     * @return the string
     */
    public static String addParameterValueMap(String url,Map<String, String> nameAndValueMap,String charsetType){
        Map<String, String[]> keyAndArrayMap = new HashMap<String, String[]>();

        if (Validator.isNotNullOrEmpty(nameAndValueMap)){
            for (Map.Entry<String, String> entry : nameAndValueMap.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                keyAndArrayMap.put(key, new String[] { value });
            }
        }
        return addParameterArrayMap(url, keyAndArrayMap, charsetType);
    }

    /**
     * 添加参数 加入含有该参数会替换掉.
     * 
     * @param uri
     *            URI 统一资源标识符 (URI),<br>
     *            如果带有? 和参数,会先被截取,最后再拼接,<br>
     *            如果不带?,则自动 增加?
     * @param paramName
     *            添加的参数名称
     * @param parameValue
     *            添加的参数值
     * @param charsetType
     *            编码
     * @return 添加参数 加入含有该参数会替换掉
     */
    public static String addParameter(URI uri,String paramName,Object parameValue,String charsetType){
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(paramName, new String[] { parameValue.toString() });
        return addParameterArrayMap(uri, map, charsetType);
    }

    /**
     * 添加参数 <br>
     * 假如含有该参数会替换掉，比如原来是a=1&a=2,现在使用a,[3,4]调用这个方法， 会返回a=3&a=4.
     * 
     * @param uri
     *            URI 统一资源标识符 (URI),<br>
     *            如果带有? 和参数,会先被截取,最后再拼接,<br>
     *            如果不带?,则自动 增加?
     * @param nameAndValueMap
     *            nameAndValueMap 类似于 request.getParameterMap
     * @param charsetType
     *            编码
     * @return 添加参数 加入含有该参数会替换掉
     */
    public static String addParameterArrayMap(URI uri,Map<String, String[]> nameAndValueMap,String charsetType){
        if (null == uri){
            throw new IllegalArgumentException("uri can not be null!");
        }
        if (Validator.isNullOrEmpty(nameAndValueMap)){
            throw new IllegalArgumentException("nameAndValueMap can not be null!");
        }
        // ***********************************************************************
        String url = uri.toString();
        String before = URIUtil.getBeforePath(url);
        // ***********************************************************************
        // getQuery() 返回此 URI 的已解码的查询组成部分。
        // getRawQuery() 返回此 URI 的原始查询组成部分。 URI 的查询组成部分（如果定义了）只包含合法的 URI 字符。
        String query = uri.getRawQuery();
        // ***********************************************************************
        Map<String, String[]> map = new LinkedHashMap<String, String[]>();
        // 传入的url不带参数的情况
        if (Validator.isNullOrEmpty(query)){
            // nothing to do
        }else{
            Map<String, String[]> originalMap = URIUtil.parseQueryToArrayMap(query, null);
            map.putAll(originalMap);
        }
        map.putAll(nameAndValueMap);
        // **************************************************************
        return URIUtil.getEncodedUrlByArrayMap(before, map, charsetType);
    }

    // ********************************removeParameter*********************************************************************

    /**
     * 删除参数.
     * 
     * @param url
     *            the url
     * @param paramName
     *            the param name
     * @param charsetType
     *            编码
     * @return the string
     */
    public static String removeParameter(String url,String paramName,String charsetType){
        URI uri = URIUtil.create(url, charsetType);
        return removeParameter(uri, paramName, charsetType);
    }

    /**
     * 删除参数.
     * 
     * @param uri
     *            the uri
     * @param paramName
     *            the param name
     * @param charsetType
     *            编码
     * @return the string
     */
    private static String removeParameter(URI uri,String paramName,String charsetType){
        List<String> paramNameList = null;
        if (Validator.isNotNullOrEmpty(paramName)){
            paramNameList = new ArrayList<String>();
            paramNameList.add(paramName);
        }
        return removeParameterList(uri, paramNameList, charsetType);
    }

    /**
     * 删除参数.
     * 
     * @param url
     *            the url
     * @param paramNameList
     *            the param name list
     * @param charsetType
     *            编码
     * @return the string
     */
    public static String removeParameterList(String url,List<String> paramNameList,String charsetType){
        URI uri = URIUtil.create(url, charsetType);
        return removeParameterList(uri, paramNameList, charsetType);
    }

    /**
     * 删除参数.
     * 
     * @param uri
     *            the uri
     * @param paramNameList
     *            the param name list
     * @param charsetType
     *            编码
     * @return the string
     */
    public static String removeParameterList(URI uri,List<String> paramNameList,String charsetType){
        if (null == uri){
            return "";
        }
        String url = uri.toString();
        // 如果 paramNameList 是null 原样返回
        if (Validator.isNullOrEmpty(paramNameList)){
            return url;
        }
        // ***********************************************************************
        String before = URIUtil.getBeforePath(url);
        // ***********************************************************************
        // 返回此 URI 的原始查询组成部分。 URI 的查询组成部分（如果定义了）只包含合法的 URI 字符。
        String query = uri.getRawQuery();
        // ***********************************************************************
        // 传入的url不带参数的情况
        if (Validator.isNullOrEmpty(query)){
            // 不带参数原样返回
            return url;
        }else{
            Map<String, String[]> map = URIUtil.parseQueryToArrayMap(query, null);
            for (String paramName : paramNameList){
                map.remove(paramName);
            }
            return URIUtil.getEncodedUrlByArrayMap(before, map, charsetType);
        }
    }

    // **************************************retentionParams********************************************************

    /**
     * url里面仅保留 指定的参数.
     * 
     * @param url
     *            the url
     * @param paramNameList
     *            the param name list
     * @param charsetType
     *            编码
     * @return the string
     */
    public static String retentionParamList(String url,List<String> paramNameList,String charsetType){
        URI uri = URIUtil.create(url, charsetType);
        return retentionParamList(uri, paramNameList, charsetType);
    }
    
    /**
     * 只是简单的将map的key value 链接起来,最终格式类似于 url 的queryString.
     * 
     * <h3>注意点:</h3>
     * 
     * <blockquote>
     * <ul>
     * <li>该方法<span style="color:red">不会执行encode操作</span>,使用原生值进行拼接</li>
     * <li>按照传入的map key顺序进行排序,不会自行自动排序转换;如有有业务需求,先行排序完传入进来</li>
     * </ul>
     * </blockquote>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * Map{@code <String, String[]>} keyAndArrayMap = new LinkedHashMap{@code <String, String[]>}();
     * 
     * keyAndArrayMap.put("province", new String[] { "江苏省", "浙江省" });
     * keyAndArrayMap.put("city", new String[] { "南通市" });
     * LOGGER.info(ParamUtil.joinArrayValueMap(keyAndArrayMap));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * {@code province=江苏省&province=浙江省&city=南通市}
     * </pre>
     * 
     * </blockquote>
     *
     * @param arrayValueMap
     *            the array value map
     * @return 如果 <code>arrayValueMap</code> 是 Null或者Empty,返回 {@link StringUtils#EMPTY}<br>
     *         否则循环 <code>arrayValueMap</code> 拼接成QueryString
     * @see #joinParamNameAndValues(String, String[])
     * @see <a href="http://www.leveluplunch.com/java/examples/build-convert-map-to-query-string/">build-convert-map-to-query-string</a>
     * @since 1.5.5
     */
    public static String toQueryStringUseArrayValueMap(Map<String, String[]> arrayValueMap){
        if (Validator.isNullOrEmpty(arrayValueMap)){
            return StringUtils.EMPTY;
        }

        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : arrayValueMap.entrySet()){
            sb.append(joinParamNameAndValues(entry.getKey(), entry.getValue()));
            if (i != arrayValueMap.size() - 1){// 最后一个& 不拼接
                sb.append(URIComponents.AMPERSAND);
            }
            ++i;
        }
        return sb.toString();
    }

    /**
     * url里面仅保留 指定的参数.
     * 
     * @param uri
     *            the uri
     * @param paramNameList
     *            the param name list
     * @param charsetType
     *            编码
     * @return the string
     */
    public static String retentionParamList(URI uri,List<String> paramNameList,String charsetType){
        if (null == uri){
            return "";
        }else{
            String url = uri.toString();
            // 如果 paramNameList 是null 原样返回
            if (Validator.isNullOrEmpty(paramNameList)){
                return url;
            }
            String before = URIUtil.getBeforePath(url);
            // ***********************************************************************
            // 返回此 URI 的原始查询组成部分。 URI 的查询组成部分（如果定义了）只包含合法的 URI 字符。
            String query = uri.getRawQuery();
            // ***********************************************************************
            // 传入的url不带参数的情况
            if (Validator.isNullOrEmpty(query)){
                // 不带参数原样返回
                return url;
            }else{
                Map<String, String[]> map = new LinkedHashMap<String, String[]>();

                Map<String, String[]> originalMap = URIUtil.parseQueryToArrayMap(query, null);

                for (String paramName : paramNameList){
                    map.put(paramName, originalMap.get(paramName));
                }
                return URIUtil.getEncodedUrlByArrayMap(before, map, charsetType);
            }
        }
    }
    
    /**
     * 将{@code a=1&b=2}这样格式的数据转换成map (如果charsetType 不是null或者empty 返回安全的 key和value).
     * 
     * <p>
     * 内部使用 {@link LinkedHashMap},map顺序依照 <code>queryString</code> 逗号分隔的顺序
     * </p>
     * 
     * <p>
     * 解析方式:参数和参数之间是以 {@code &} 分隔, 参数的key和value 是以 = 号分隔
     * </p>
     * 
     * <h3>示例1:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * LOGGER.info(JsonUtil.format(ParamUtil.toSafeArrayValueMap("{@code a=1&b=2&a=5}", CharsetType.UTF8)));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     {"a": [
                 "1",
                 "5"
             ],
             "b": ["2"]
      }
     * </pre>
     * 
     * <hr>
     * 
     * <pre class="code">
     * LOGGER.info(JsonUtil.format(ParamUtil.toSafeArrayValueMap("{@code a=&b=2&a}", CharsetType.UTF8)));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     {"a": [
             "",
             ""
         ],
      "b": ["2"]
     }
     * </pre>
     * 
     * </blockquote>
     *
     * @param queryString
     *            {@code a=1&b=2}类型的数据,支持{@code a=1&a=1}的形式, 返回map的值是数组
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>queryString</code> 是null或者empty,返回 {@link Collections#emptyMap()}<br>
     * @see org.apache.commons.lang3.ArrayUtils#add(Object[], Object)
     * @see com.feilong.core.lang.StringUtil#split(String, String)
     * @since 1.4.0
     */
    public static Map<String, String[]> toSafeArrayValueMap(String queryString,String charsetType){
        if (Validator.isNullOrEmpty(queryString)){
            return Collections.emptyMap();
        }

        String[] nameAndValueArray = StringUtil.split(queryString, URIComponents.AMPERSAND);
        int length = nameAndValueArray.length;

        Map<String, String[]> safeArrayValueMap = MapUtil.newLinkedHashMap(length);//使用 LinkedHashMap 保证元素的顺序
        for (int i = 0; i < length; ++i){
            String[] tempArray = nameAndValueArray[i].split("=", 2);

            String key = decodeAndEncode(tempArray[0], charsetType);
            String value = tempArray.length == 2 ? tempArray[1] : StringUtils.EMPTY;//有可能参数中,只有名字没有值或者值是空,处理的时候不能遗失掉
            value = decodeAndEncode(value, charsetType);

            safeArrayValueMap.put(key, ArrayUtils.add(safeArrayValueMap.get(key), value));
        }
        return safeArrayValueMap;
    }
    
    /**
     * 将map混合成 queryString.
     * 
     * <p>
     * 返回的queryString参数顺序,按照传入的singleValueMap key顺序排列,可以考虑传入 {@link TreeMap},{@link LinkedHashMap}等以适应不同业务的需求
     * </p>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * 对于以下的map,
     * 
     * <pre class="code">
     * Map{@code <String, String[]>} keyAndArrayMap = new HashMap{@code <String, String[]>}();
     * keyAndArrayMap.put("name", new String[] { "jim", "feilong", "鑫哥" });
     * keyAndArrayMap.put("age", new String[] { "18" });
     * keyAndArrayMap.put("love", new String[] { "sanguo" });
     * </pre>
     * 
     * 如果使用的是:
     * 
     * <pre class="code">
     * LOGGER.info(ParamUtil.toSafeQueryString(keyAndArrayMap, CharsetType.UTF8));
     * </pre>
     * 
     * 那么返回:
     * 
     * <pre class="code">
     * {@code love=sanguo&age=18&name=jim&name=feilong&name=%E9%91%AB%E5%93%A5}
     * </pre>
     * 
     * 如果使用的是:
     * 
     * <pre class="code">
     * LOGGER.info(ParamUtil.toSafeQueryString(keyAndArrayMap, null));
     * </pre>
     * 
     * 那么返回:
     * 
     * <pre class="code">
     * {@code love=sanguo&age=18&name=jim&name=feilong&name=鑫哥}
     * </pre>
     * 
     * </blockquote>
     *
     * @param arrayValueMap
     *            类似于 <code>request.getParamMap</code>
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>arrayValueMap</code> 是null或者empty,返回 {@link StringUtils#EMPTY}<br>
     * @see #toQueryStringUseArrayValueMap(Map)
     * @since 1.4.0
     */
    public static String toSafeQueryString(Map<String, String[]> arrayValueMap,String charsetType){
        return toQueryStringUseArrayValueMap(toSafeArrayValueMap(arrayValueMap, charsetType));
    }
    
    /**
     * 添加 parameter array value map.
     * 
     * <p>
     * 如果 <code>queryString</code> 参数不为空,那么会解析成map,此后再拼接 <code>arrayValueMap</code>;<br>
     * 内部使用 {@link LinkedHashMap},保持map元素顺序
     * </p>
     * 
     * @param uriString
     *            the uri string
     * @param queryString
     *            the query
     * @param arrayValueMap
     *            the name and array value map
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return the string
     * @since 1.4.0
     */
    private static String addParameterArrayValueMap(
                    String uriString,
                    String queryString,
                    Map<String, String[]> arrayValueMap,
                    String charsetType){
        Map<String, String[]> safeArrayValueMap = ObjectUtils.defaultIfNull(arrayValueMap, Collections.<String, String[]> emptyMap());

        Map<String, String[]> arrayParamValuesMap = MapUtil.newLinkedHashMap(safeArrayValueMap.size());
        //先提取queryString map
        if (Validator.isNotNullOrEmpty(queryString)){
            arrayParamValuesMap.putAll(toSafeArrayValueMap(queryString, null));
        }
        arrayParamValuesMap.putAll(safeArrayValueMap);
        return combineUrl(URIUtil.getFullPathWithoutQueryString(uriString), arrayParamValuesMap, charsetType);
    }
    
    /**
     * 浏览器传递queryString()参数差别(浏览器兼容问题);chrome会将query进行 encoded再发送请求;而ie原封不动的发送.
     * 
     * <p>
     * 由于暂时不能辨别是否encoded过,所以先强制decode再encode;
     * </p>
     * 
     * <p>
     * 此处不能先转decode(query,charsetType),参数就是想传 =是转义符
     * </p>
     *
     * @param value
     *            the value
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>value</code>是 null或者empty,返回 {@link StringUtils#EMPTY}<br>
     *         如果<code>charsetType</code>是 null或者empty,直接返回 <code>value</code><br>
     *         否则先 {@link URIUtil#decode(String, String)} 再 {@link URIUtil#encode(String, String)}值
     * @see <a
     *      href="http://stackoverflow.com/questions/15004593/java-request-getquerystring-value-different-between-chrome-and-ie-browser">
     *      java-request-getquerystring-value-different-between-chrome-and-ie-browser</a>
     * @since 1.4.0
     */
    private static String decodeAndEncode(String value,String charsetType){
        if (Validator.isNullOrEmpty(value)){
            return StringUtils.EMPTY;
        }
        return Validator.isNullOrEmpty(charsetType) ? value : URIUtil.encode(URIUtil.decode(value, charsetType), charsetType);
    }
    
    /**
     * 拼接url.
     *
     * @param beforePathWithoutQueryString
     *            the before path without query string
     * @param arrayValueMap
     *            the array value map
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>beforePathWithoutQueryString</code> 是null或者empty,返回 {@link StringUtils#EMPTY}<br>
     *         如果<code>arrayValueMap</code> 是null或者empty,返回 <code>beforePathWithoutQueryString</code>
     * @since 1.4.0
     */
    private static String combineUrl(String beforePathWithoutQueryString,Map<String, String[]> arrayValueMap,String charsetType){
        if (Validator.isNullOrEmpty(beforePathWithoutQueryString)){
            return StringUtils.EMPTY;
        }
        if (Validator.isNullOrEmpty(arrayValueMap)){//没有参数 直接return
            return beforePathWithoutQueryString;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(beforePathWithoutQueryString);
        sb.append(URIComponents.QUESTIONMARK);
        sb.append(toSafeQueryString(arrayValueMap, charsetType));

        return sb.toString();
    }
    
    /**
     * To safe array value map.
     * 
     * <p>
     * 内部使用 {@link LinkedHashMap},保持map元素顺序
     * </p>
     *
     * @param arrayValueMap
     *            the array value map
     * @param charsetType
     *            何种编码, {@link CharsetType}<br>
     *            <span style="color:green">如果是null或者 empty,那么参数部分原样返回,自己去处理兼容性问题</span><br>
     *            否则会先解码,再加码,因为ie浏览器和chrome浏览器 url中访问路径 ,带有中文情况下不一致
     * @return 如果 <code>arrayValueMap</code> 是null或者empty,返回 {@link Collections#emptyMap()}<br>
     */
    private static Map<String, String[]> toSafeArrayValueMap(Map<String, String[]> arrayValueMap,String charsetType){
        if (Validator.isNullOrEmpty(arrayValueMap)){
            return Collections.emptyMap();
        }
        Map<String, String[]> safeArrayValueMap = MapUtil.newLinkedHashMap(arrayValueMap.size()); //使用 LinkedHashMap,保持map元素顺序
        for (Map.Entry<String, String[]> entry : arrayValueMap.entrySet()){
            String key = entry.getKey();
            String[] paramValues = entry.getValue();
            if (Validator.isNullOrEmpty(paramValues)){
                LOGGER.warn("the param key:[{}] value is null", key);
                paramValues = ArrayUtils.EMPTY_STRING_ARRAY;//赋予 empty数组,为了下面的转换
            }
            safeArrayValueMap.put(decodeAndEncode(key, charsetType), toSafeValueArray(paramValues, charsetType));
        }
        return safeArrayValueMap;
    }
    
    /**
     * To safe value array.
     *
     * @param paramValues
     *            the param values
     * @param charsetType
     *            the charset type
     * @return the string[]
     * @since 1.6.2
     */
    private static String[] toSafeValueArray(String[] paramValues,String charsetType){
        if (Validator.isNullOrEmpty(charsetType)){
            return paramValues;
        }
        List<String> paramValueList = new ArrayList<String>();
        for (String value : paramValues){
            paramValueList.add(decodeAndEncode(value, charsetType));
        }
        return ConvertUtil.toArray(paramValueList, String.class);
    }
    
    /**
     * 将参数和多值链接起来.
     * 
     * <p>
     * 比如,参数名字 {@code paramName=name}, {@code paramValues 为 zhangfei,guanyu},那么返回值是{@code name=zhangfei&name=guanyu}
     * </p>
     * 
     * <h3>注意:</h3>
     * <blockquote>
     * <ol>
     * <li>paramName 和每个值 都会调用 {@link StringUtils#defaultString(String)}转换后才进行拼接</li>
     * </ol>
     * </blockquote>
     *
     * @param paramName
     *            参数名字
     * @param paramValues
     *            参数多值
     * @return the string
     * @see java.lang.AbstractStringBuilder#append(String)
     * @see org.apache.commons.lang3.StringUtils#defaultString(String)
     * @see "org.springframework.web.servlet.view.RedirectView#appendQueryProperties(StringBuilder,Map, String)"
     * @since 1.4.0
     */
    private static String joinParamNameAndValues(String paramName,String[] paramValues){
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = paramValues.length; i < j; ++i){
            //注意:如果 value 是null ,StringBuilder将拼接 "null" 字符串, 详见  java.lang.AbstractStringBuilder#append(String)
            sb.append(StringUtils.defaultString(paramName)).append("=").append(StringUtils.defaultString(paramValues[i]));
            if (i != j - 1){// 最后一个& 不拼接
                sb.append(URIComponents.AMPERSAND);
            }
        }
        return sb.toString();
    }
}
