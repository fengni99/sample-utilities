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
package com.mdl.sample.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mdl.sample.bean.ConvertUtil;
import com.mdl.sample.util.comparator.PropertyComparator;


/**
 * {@link Map}工具类.
 *
 * @author feilong
 * @version 1.0.0 Sep 8, 2012 8:02:44 PM
 * @see org.apache.commons.collections.MapUtils
 * @since 1.0.0
 */
public final class MapUtil{

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MapUtil.class);

    /** Don't let anyone instantiate this class. */
    private MapUtil(){
        //AssertionError不是必须的. 但它可以避免不小心在类的内部调用构造器. 保证该类在任何情况下都不会被实例化.
        //see 《Effective Java》 2nd
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }
    
    /**
     * 将单值的参数map转成多值的参数map.
     * 
     * <p style="color:green">
     * 返回的是 {@link LinkedHashMap},保证顺序和 参数 <code>singleValueMap</code>顺序相同
     * </p>
     * 
     * <p>
     * 和该方法正好相反的是 {@link #toSingleValueMap(Map)}
     * </p>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre class="code">
     * Map{@code <String, String>} singleValueMap = new LinkedHashMap{@code <String, String>}();
     * 
     * singleValueMap.put("province", "江苏省");
     * singleValueMap.put("city", "南通市");
     * 
     * LOGGER.info(JsonUtil.format(ParamUtil.toArrayValueMap(singleValueMap)));
     * </pre>
     * 
     * 返回:
     * 
     * <pre class="code">
     * {
     * "province": ["江苏省"],
     * "city": ["南通市"]
     * }
     * </pre>
     * 
     * </blockquote>
     *
     * @param <K>
     *            the key type
     * @param singleValueMap
     *            the name and value map
     * @return 如果参数 <code>singleValueMap</code> 是null或者empty,那么返回 {@link Collections#emptyMap()}<br>
     *         否则迭代 <code>singleValueMap</code> 将value转成数组,返回新的 <code>arrayValueMap</code>
     * @since 1.6.2
     */
    public static <K> Map<K, String[]> toArrayValueMap(Map<K, String> singleValueMap){
        if (Validator.isNullOrEmpty(singleValueMap)){
            return Collections.emptyMap();
        }
        Map<K, String[]> arrayValueMap = newLinkedHashMap(singleValueMap.size());//保证顺序和参数singleValueMap顺序相同
        for (Map.Entry<K, String> entry : singleValueMap.entrySet()){
            arrayValueMap.put(entry.getKey(), ConvertUtil.toArray(entry.getValue()));//注意此处的Value不要声明成V,否则会变成Object数组
        }
        return arrayValueMap;
    }

    /**
     * 取到指定keys的value,连接起来,如果value是null or empty将会被排除,不参与拼接.
     *
     * @param map
     *            the map
     * @param includeKeys
     *            包含的key
     * @return the mer data
     * @since 1.2.1
     */
    public static String joinKeysValue(Map<String, String> map,String[] includeKeys){
        if (Validator.isNullOrEmpty(map)) {
            throw new NullPointerException("map can't be null/empty!");
        }
        StringBuilder sb = new StringBuilder();
        //有顺序的参数
        for (String key : includeKeys){
            String value = map.get(key);

            //不判断的话,null会输出成null字符串
            if (Validator.isNotNullOrEmpty(value)) {
                sb.append(value);
            }
        }
        String merData = sb.toString();
        return merData;
    }

    /**
     * 获得 一个map 中的 按照指定的key 整理成新的map.
     *
     * @param <K>
     *            the key type
     * @param <T>
     *            the generic type
     * @param map
     *            the map
     * @param keys
     *            指定key,如果key 不在map key 里面 ,则返回的map 中忽略该key
     * @return the sub map<br>
     *         if (Validator.isNullOrEmpty(keys)) 返回map<br>
     */
    public static <K, T> Map<K, T> getSubMap(Map<K, T> map,K[] keys){
        if (Validator.isNullOrEmpty(map)) {
            throw new NullPointerException("the map is null or empty!");
        }
        if (Validator.isNullOrEmpty(keys)) {
            return map;
        }
        Map<K, T> returnMap = new HashMap<K, T>();

        for (K key : keys){
            if (map.containsKey(key)) {
                returnMap.put(key, map.get(key));
            }else{
                LOGGER.warn("map don't contains key:[{}]", key);
            }
        }
        return returnMap;
    }

    /**
     * 获得 sub map(去除不需要的keys).
     *
     * @param <K>
     *            the key type
     * @param <T>
     *            the generic type
     * @param map
     *            the map
     * @param excludeKeys
     *            the keys
     * @return the sub map<br>
     *         if (Validator.isNullOrEmpty(keys)) 返回map<br>
     * @since 1.0.9
     */
    public static <K, T> Map<K, T> getSubMapExcludeKeys(Map<K, T> map,K[] excludeKeys){
        if (Validator.isNullOrEmpty(map)) {
            throw new NullPointerException("the map is null or empty!");
        }
        if (Validator.isNullOrEmpty(excludeKeys)) {
            return map;
        }

        Map<K, T> returnMap = new HashMap<K, T>(map);

        for (K key : excludeKeys){
            if (map.containsKey(key)) {
                returnMap.remove(key);
            }else{
                LOGGER.warn("map don't contains key:[{}]", key);
            }
        }
        return returnMap;
    }

    //*******************************排序****************************************************
    /**
     * Sort by key asc.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map
     * @return the map< k, v>
     * @see java.util.TreeMap#TreeMap(Map)
     * @since 1.2.0
     */
    public static <K, V> Map<K, V> sortByKeyAsc(Map<K, V> map){
        if (Validator.isNullOrEmpty(map)) {
            throw new NullPointerException("map can't be null/empty!");
        }
        return new TreeMap<K, V>(map);
    }

    /**
     * Sort by key desc.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map
     * @return the map< k, v>
     * @see org.apache.commons.collections.comparators.ReverseComparator#ReverseComparator(Comparator)
     * @see PropertyComparator#PropertyComparator(String)
     * @since 1.2.0
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> sortByKeyDesc(Map<K, V> map){
        if (Validator.isNullOrEmpty(map)) {
            throw new NullPointerException("map can't be null/empty!");
        }
        return sort(map, new ReverseComparator(new PropertyComparator<Map.Entry<K, V>>("key")));
    }

    /**
     * 根据value 来顺序排序（asc）.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map
     * @return the map< k, v>
     * @see PropertyComparator#PropertyComparator(String)
     * @see java.util.Map.Entry
     * @see #sortByValueDesc(Map)
     * @since 1.2.0
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValueAsc(Map<K, V> map){
        return sort(map, new PropertyComparator<Map.Entry<K, V>>("value"));
    }

    /**
     * 根据value 来倒序排序（desc）.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map
     * @return the map< k, v>
     * @see org.apache.commons.collections.comparators.ReverseComparator#ReverseComparator(Comparator)
     * @see PropertyComparator#PropertyComparator(String)
     * @see java.util.Map.Entry
     * @see #sortByValueAsc(Map)
     * @since 1.2.0
     */
    @SuppressWarnings("unchecked")
    public static <K, V extends Comparable<V>> Map<K, V> sortByValueDesc(Map<K, V> map){
        return sort(map, new ReverseComparator(new PropertyComparator<Map.Entry<K, V>>("value")));
    }

    /**
     * 使用 基于 {@link java.util.Map.Entry} 的 <code>mapEntryComparator</code> 来对 <code>map</code>进行排序.
     * 
     * <p>
     * 由于是对{@link java.util.Map.Entry}排序的, 既可以按照key来排序,也可以按照value来排序哦
     * </p>
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map
     * @param mapEntryComparator
     *            基于 {@link java.util.Map.Entry} 的 {@link Comparator}
     * @return 排序之后的map
     * @since 1.2.0
     */
    public static <K, V> Map<K, V> sort(Map<K, V> map,Comparator<Map.Entry<K, V>> mapEntryComparator){

        if (Validator.isNullOrEmpty(map)) {
            throw new NullPointerException("the map is null or empty!");
        }

        if (Validator.isNullOrEmpty(mapEntryComparator)) {
            throw new NullPointerException("mapEntryComparator is null or empty!");
        }

        //**********************************************************

        final int size = map.size();
        List<Map.Entry<K, V>> mapEntryList = new ArrayList<Map.Entry<K, V>>(size);
        for (Map.Entry<K, V> entry : map.entrySet()){
            mapEntryList.add(entry);
        }

        //**********************排序************************************
        Collections.sort(mapEntryList, mapEntryComparator);

        //**********************************************************
        Map<K, V> returnMap = new LinkedHashMap<K, V>(size);

        for (Map.Entry<K, V> entry : mapEntryList){
            K key = entry.getKey();
            V value = entry.getValue();
            returnMap.put(key, value);
        }

        return returnMap;
    }
    
    /**
     * Creates a {@code LinkedHashMap} instance, with a high enough "initial capacity" that it <i>should</i> hold {@code expectedSize}
     * elements without growth. This behavior cannot be broadly guaranteed, but it is observed to be true for OpenJDK 1.7. <br>
     * It also can't be guaranteed that the method isn't inadvertently <i>oversizing</i> the returned map.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param expectedSize
     *            the number of entries you expect to add to the returned map
     * @return a new, empty {@code LinkedHashMap} with enough capacity to hold {@code expectedSize} entries without resizing
     * @see "com.google.common.collect.Maps#newLinkedHashMapWithExpectedSize(int)"
     * @see java.util.LinkedHashMap#LinkedHashMap(int)
     * @since 1.7.1
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize){
        return new LinkedHashMap<K, V>(toInitialCapacity(expectedSize));
    }
    
    /**
     * 将<code>size</code>转成 <code>initialCapacity</code> (for {@link java.util.HashMap}).
     * 
     * <p>
     * 适合于明确知道 hashmap size,现在需要初始化的情况
     * </p>
     *
     * @param size
     *            map的 size
     * @return the int
     * @see <a href="http://www.iteye.com/topic/1134016">java hashmap，如果确定只装载100个元素，new HashMap(?)多少是最佳的，why？ </a>
     * @see <a href=
     *      "http://stackoverflow.com/questions/30220820/difference-between-new-hashmapint-and-guava-maps-newhashmapwithexpectedsizein">
     *      Difference between new HashMap(int) and guava Maps.newHashMapWithExpectedSize(int)</a>
     * @see <a href="http://stackoverflow.com/questions/15844035/best-hashmap-initial-capacity-while-indexing-a-list">Best HashMap initial
     *      capacity while indexing a List</a>
     * @see java.util.HashMap#HashMap(Map)
     * @see "com.google.common.collect.Maps#capacity(int)"
     * @see java.util.HashMap#inflateTable(int)
     * @see org.apache.commons.collections4.map.AbstractHashedMap#calculateNewCapacity(int)
     * @since 1.7.1
     */
    private static int toInitialCapacity(int size){
        Validate.isTrue(size >= 0, "size :[%s] must >=0", size);

        //借鉴了 google guava 的实现,不过 guava 不同版本实现不同
        //guava 19 (int) (expectedSize / 0.75F + 1.0F)
        //guava 18  expectedSize + expectedSize / 3
        //google-collections 1.0  Math.max(expectedSize * 2, 16)

        //This is the calculation used in JDK8 to resize when a putAll happens it seems to be the most conservative calculation we can make.  
        return (int) (size / 0.75f) + 1;//0.75 is the default load factor
    }
}
